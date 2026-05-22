package com.parttime.enterprise.service.impl;

import com.parttime.enterprise.mapper.AttendanceRecordMapper;
import com.parttime.enterprise.mapper.BalanceTransactionMapper;
import com.parttime.enterprise.mapper.ScheduleShiftMapper;
import com.parttime.enterprise.mapper.SettlementBillMapper;
import com.parttime.enterprise.mapper.WorkerBalanceMapper;
import com.parttime.enterprise.mapper.WorkerSyncMapper;
import com.parttime.enterprise.pojo.entity.AttendanceRecord;
import com.parttime.enterprise.pojo.entity.BalanceTransaction;
import com.parttime.enterprise.pojo.entity.ScheduleShift;
import com.parttime.enterprise.pojo.entity.SettlementBill;
import com.parttime.enterprise.pojo.entity.WorkerBalance;
import com.parttime.enterprise.pojo.vo.PageVO;
import com.parttime.enterprise.pojo.vo.SettlementBillVO;
import com.parttime.enterprise.service.SettlementService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class SettlementServiceImpl implements SettlementService {

    @Resource
    private SettlementBillMapper settlementBillMapper;

    @Resource
    private AttendanceRecordMapper attendanceRecordMapper;

    @Resource
    private ScheduleShiftMapper scheduleShiftMapper;

    @Resource
    private WorkerSyncMapper workerSyncMapper;

    @Resource
    private WorkerBalanceMapper workerBalanceMapper;

    @Resource
    private BalanceTransactionMapper balanceTransactionMapper;

    private static final DateTimeFormatter SERIAL_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final Random RANDOM = new Random();

    @Override
    public PageVO<SettlementBillVO> listBills(Long companyId, String workerName, LocalDate dateFrom, LocalDate dateTo, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        List<SettlementBill> bills = settlementBillMapper.findByCompanyId(companyId, workerName, dateFrom, dateTo, offset, pageSize);
        long total = settlementBillMapper.countByCompanyId(companyId, workerName, dateFrom, dateTo);
        List<SettlementBillVO> voList = bills.stream().map(this::toVO).collect(Collectors.toList());
        return new PageVO<>(voList, total);
    }

    @Override
    @Transactional
    public void payFromAttendanceRecords(List<Long> attendanceRecordIds, Long companyId) {
        for (Long arId : attendanceRecordIds) {
            AttendanceRecord ar = attendanceRecordMapper.findById(arId)
                    .orElseThrow(() -> new RuntimeException("Attendance record not found: " + arId));
            if ("PAID".equals(ar.getSettlementStatus())) continue;

            ScheduleShift shift = scheduleShiftMapper.findById(ar.getShiftId())
                    .orElseThrow(() -> new RuntimeException("Schedule shift not found: " + ar.getShiftId()));

            Long workerId = ar.getWorkerId() != null ? ar.getWorkerId() : shift.getWorkerId();
            String workerName = workerSyncMapper.findWorkerNameById(workerId);
            BigDecimal actualPay = ar.getPayablePay() != null ? ar.getPayablePay() : ar.getScheduledPay();

            String serialNo = generateSerialNumber();

            SettlementBill bill = new SettlementBill();
            bill.setCompanyId(companyId);
            bill.setJobId(ar.getJobId() != null ? ar.getJobId() : shift.getJobId());
            bill.setShiftId(ar.getShiftId());
            bill.setWorkerId(workerId);
            bill.setWorkerName(workerName);
            bill.setShiftDate(shift.getShiftDate());
            bill.setStartTime(shift.getStartTime());
            bill.setEndTime(shift.getEndTime());
            bill.setTotalHours(ar.getTotalHours());
            bill.setRateType(shift.getSalaryType());
            bill.setRateAmount(shift.getSalaryAmount());
            bill.setScheduledPay(ar.getScheduledPay());
            bill.setActualPay(actualPay);
            bill.setSerialNumber(serialNo);
            bill.setStatus("PAID");
            bill.setPaidAt(LocalDateTime.now());
            settlementBillMapper.insert(bill);

            WorkerBalance wb = workerBalanceMapper.findByWorkerId(workerId);
            if (wb == null) {
                workerBalanceMapper.upsert(workerId, actualPay, actualPay, BigDecimal.ZERO);
            } else {
                workerBalanceMapper.upsert(workerId,
                        wb.getBalance().add(actualPay),
                        wb.getTotalEarned().add(actualPay),
                        wb.getTotalWithdrawn());
            }

            BalanceTransaction bt = new BalanceTransaction();
            bt.setWorkerId(workerId);
            bt.setAmount(actualPay);
            bt.setType("EARNINGS");
            bt.setRelatedBillId(bill.getId());
            bt.setDescription("结算收入: " + workerName + " " + shift.getShiftDate());
            balanceTransactionMapper.insert(bt);

            ar.setSettlementStatus("PAID");
            attendanceRecordMapper.update(ar);
        }
    }

    @Override
    @Transactional
    public void unsettle(Long attendanceRecordId, Long companyId) {
        AttendanceRecord ar = attendanceRecordMapper.findById(attendanceRecordId)
                .orElseThrow(() -> new RuntimeException("Attendance record not found: " + attendanceRecordId));
        if (!"PAID".equals(ar.getSettlementStatus())) {
            throw new RuntimeException("Record is not settled");
        }

        SettlementBill bill = settlementBillMapper.findByShiftId(ar.getShiftId())
                .orElseThrow(() -> new RuntimeException("Settlement bill not found for this record"));

        Long workerId = ar.getWorkerId() != null ? ar.getWorkerId() : bill.getWorkerId();
        BigDecimal actualPay = bill.getActualPay();

        WorkerBalance wb = workerBalanceMapper.findByWorkerId(workerId);
        if (wb == null || wb.getBalance().compareTo(actualPay) < 0) {
            throw new RuntimeException("Cannot unsettle: worker has withdrawn the settled amount");
        }

        bill.setStatus("REFUNDED");
        settlementBillMapper.updateBillStatus(bill.getId(), "REFUNDED");

        workerBalanceMapper.upsert(workerId,
                wb.getBalance().subtract(actualPay),
                wb.getTotalEarned().subtract(actualPay),
                wb.getTotalWithdrawn());

        BalanceTransaction bt = new BalanceTransaction();
        bt.setWorkerId(workerId);
        bt.setAmount(actualPay.negate());
        bt.setType("REFUND");
        bt.setRelatedBillId(bill.getId());
        bt.setDescription("撤回结算: " + bill.getWorkerName() + " " + bill.getShiftDate());
        balanceTransactionMapper.insert(bt);

        ar.setSettlementStatus("UNPAID");
        attendanceRecordMapper.update(ar);
    }

    private SettlementBillVO toVO(SettlementBill bill) {
        SettlementBillVO vo = new SettlementBillVO();
        BeanUtils.copyProperties(bill, vo);
        return vo;
    }

    private synchronized String generateSerialNumber() {
        String ts = LocalDateTime.now().format(SERIAL_FMT);
        int seq = RANDOM.nextInt(10000);
        return "SETT" + ts + String.format("%04d", seq);
    }
}
