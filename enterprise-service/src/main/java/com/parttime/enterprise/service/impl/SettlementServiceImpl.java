package com.parttime.enterprise.service.impl;

import com.parttime.enterprise.mapper.AttendanceRecordMapper;
import com.parttime.enterprise.mapper.BalanceTransactionMapper;
import com.parttime.enterprise.mapper.ScheduleShiftMapper;
import com.parttime.enterprise.mapper.WorkerBalanceMapper;
import com.parttime.enterprise.mapper.WorkerBatchMapper;
import com.parttime.enterprise.mapper.WorkerNotificationMapper;
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
    private WorkerBatchMapper workerBatchMapper;

    @Resource
    private WorkerBalanceMapper workerBalanceMapper;

    @Resource
    private BalanceTransactionMapper balanceTransactionMapper;

    @Resource
    private EnterpriseBalanceService enterpriseBalanceService;
    @Resource
    private WorkerNotificationMapper workerNotificationMapper;

    @Override
    @Transactional
    public void payFromAttendanceRecords(List<Long> attendanceRecordIds, Long companyId) {
        if (attendanceRecordIds == null || attendanceRecordIds.isEmpty()) return;

        List<AttendanceRecord> allRecords = attendanceRecordMapper.findByIds(attendanceRecordIds);
        if (allRecords.isEmpty()) return;

        List<Long> shiftIds = allRecords.stream().map(AttendanceRecord::getShiftId).distinct().toList();
        List<ScheduleShift> shifts = scheduleShiftMapper.findByIds(shiftIds);
        java.util.Map<Long, ScheduleShift> shiftMap = shifts.stream()
                .collect(java.util.stream.Collectors.toMap(ScheduleShift::getId, s -> s));

        java.util.Map<Long, String> workerNames = new java.util.HashMap<>();
        java.util.Map<Long, WorkerBalance> workerBalances = new java.util.HashMap<>();
        List<Long> workerIds = allRecords.stream()
                .map(ar -> ar.getWorkerId() != null ? ar.getWorkerId() : shiftMap.get(ar.getShiftId()).getWorkerId())
                .distinct().toList();
        if (!workerIds.isEmpty()) {
            workerBatchMapper.findWorkerNamesByIds(workerIds).forEach(m -> workerNames.put((Long) m.get("id"), (String) m.get("name")));
            workerBalanceMapper.findByWorkerIds(workerIds).forEach(wb -> workerBalances.put(wb.getWorkerId(), wb));
        }

        BigDecimal totalPay = BigDecimal.ZERO;
        for (AttendanceRecord ar : allRecords) {
            if ("PAID".equals(ar.getSettlementStatus())) continue;

            ScheduleShift shift = shiftMap.get(ar.getShiftId());
            if (shift == null) throw new RuntimeException("Schedule shift not found: " + ar.getShiftId());

            Long workerId = ar.getWorkerId() != null ? ar.getWorkerId() : shift.getWorkerId();
            String workerName = workerNames.getOrDefault(workerId, "未知工人");
            BigDecimal actualPay = ar.getPayablePay() != null ? ar.getPayablePay() : ar.getScheduledPay();

            WorkerBalance wb = workerBalances.get(workerId);
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
            workerNotificationMapper.insertWorkerNotification(workerId, "EARNINGS_SETTLED", "finance",
                    "收入到账", "您有一笔兼职收入" + actualPay + "元已到账", "ATTENDANCE", ar.getId());

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
