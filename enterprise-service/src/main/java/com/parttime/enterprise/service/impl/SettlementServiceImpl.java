package com.parttime.enterprise.service.impl;

import com.parttime.enterprise.mapper.AttendanceRecordMapper;
import com.parttime.enterprise.mapper.BalanceTransactionMapper;
import com.parttime.enterprise.mapper.ScheduleShiftMapper;
import com.parttime.enterprise.mapper.WorkerBalanceMapper;
import com.parttime.enterprise.mapper.WorkerSyncMapper;
import com.parttime.enterprise.pojo.entity.AttendanceRecord;
import com.parttime.enterprise.pojo.entity.BalanceTransaction;
import com.parttime.enterprise.pojo.entity.ScheduleShift;
import com.parttime.enterprise.pojo.entity.WorkerBalance;
import com.parttime.enterprise.service.EnterpriseBalanceService;
import com.parttime.enterprise.service.SettlementService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

@Service
public class SettlementServiceImpl implements SettlementService {

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

    @Resource
    private EnterpriseBalanceService enterpriseBalanceService;

    @Override
    @Transactional
    public void payFromAttendanceRecords(List<Long> attendanceRecordIds, Long companyId) {
        BigDecimal totalPay = BigDecimal.ZERO;
        for (Long arId : attendanceRecordIds) {
            AttendanceRecord ar = attendanceRecordMapper.findById(arId)
                    .orElseThrow(() -> new RuntimeException("Attendance record not found: " + arId));
            if ("PAID".equals(ar.getSettlementStatus())) continue;

            ScheduleShift shift = scheduleShiftMapper.findById(ar.getShiftId())
                    .orElseThrow(() -> new RuntimeException("Schedule shift not found: " + ar.getShiftId()));

            Long workerId = ar.getWorkerId() != null ? ar.getWorkerId() : shift.getWorkerId();
            String workerName = workerSyncMapper.findWorkerNameById(workerId);
            BigDecimal actualPay = ar.getPayablePay() != null ? ar.getPayablePay() : ar.getScheduledPay();

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
            bt.setRelatedAttendanceRecordId(ar.getId());
            bt.setDescription("结算收入: " + workerName + " " + shift.getShiftDate());
            balanceTransactionMapper.insert(bt);

            ar.setSettlementStatus("PAID");
            attendanceRecordMapper.update(ar);

            totalPay = totalPay.add(actualPay);
        }

        if (totalPay.compareTo(BigDecimal.ZERO) > 0) {
            enterpriseBalanceService.deduct(companyId, totalPay, null, "批量结算: " + totalPay + "元");
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

        BalanceTransaction earnings = balanceTransactionMapper.findByAttendanceRecordIdAndType(attendanceRecordId, "EARNINGS")
                .orElseThrow(() -> new RuntimeException("Earnings transaction not found for this record"));

        ScheduleShift shift = scheduleShiftMapper.findById(ar.getShiftId())
                .orElseThrow(() -> new RuntimeException("Schedule shift not found: " + ar.getShiftId()));

        Long workerId = ar.getWorkerId() != null ? ar.getWorkerId() : earnings.getWorkerId();
        String workerName = workerSyncMapper.findWorkerNameById(workerId);
        BigDecimal actualPay = earnings.getAmount();

        WorkerBalance wb = workerBalanceMapper.findByWorkerId(workerId);
        if (wb == null || wb.getBalance().compareTo(actualPay) < 0) {
            throw new RuntimeException("Cannot unsettle: worker has withdrawn the settled amount");
        }

        workerBalanceMapper.upsert(workerId,
                wb.getBalance().subtract(actualPay),
                wb.getTotalEarned().subtract(actualPay),
                wb.getTotalWithdrawn());

        BalanceTransaction bt = new BalanceTransaction();
        bt.setWorkerId(workerId);
        bt.setAmount(actualPay.negate());
        bt.setType("REFUND");
        bt.setRelatedAttendanceRecordId(attendanceRecordId);
        bt.setDescription("撤回结算: " + workerName + " " + shift.getShiftDate());
        balanceTransactionMapper.insert(bt);

        enterpriseBalanceService.refund(companyId, actualPay, null, "撤回结算退款: " + workerName + " " + shift.getShiftDate());

        ar.setSettlementStatus("UNPAID");
        attendanceRecordMapper.update(ar);
    }
}
