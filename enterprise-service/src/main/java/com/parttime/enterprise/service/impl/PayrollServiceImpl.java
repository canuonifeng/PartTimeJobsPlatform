package com.parttime.enterprise.service.impl;

import com.parttime.enterprise.dao.AttendanceRecordDao;
import com.parttime.enterprise.dao.JobDao;
import com.parttime.enterprise.dao.PayrollBatchDao;
import com.parttime.enterprise.dao.PayrollItemDao;
import com.parttime.enterprise.dao.ScheduleShiftDao;
import com.parttime.enterprise.pojo.cmd.PayrollBatchCmd;
import com.parttime.enterprise.pojo.entity.AttendanceRecord;
import com.parttime.enterprise.pojo.entity.JobRate;
import com.parttime.enterprise.pojo.entity.PayrollBatch;
import com.parttime.enterprise.pojo.entity.PayrollItem;
import com.parttime.enterprise.pojo.entity.ScheduleShift;
import com.parttime.enterprise.pojo.vo.PayrollBatchVO;
import com.parttime.enterprise.pojo.vo.PayrollItemVO;
import com.parttime.enterprise.service.PayrollService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PayrollServiceImpl implements PayrollService {

    private final PayrollBatchDao payrollBatchDao;
    private final PayrollItemDao payrollItemDao;
    private final ScheduleShiftDao scheduleShiftDao;
    private final AttendanceRecordDao attendanceRecordDao;
    private final JobDao jobDao;

    public PayrollServiceImpl(PayrollBatchDao payrollBatchDao,
                              PayrollItemDao payrollItemDao,
                              ScheduleShiftDao scheduleShiftDao,
                              AttendanceRecordDao attendanceRecordDao,
                              JobDao jobDao) {
        this.payrollBatchDao = payrollBatchDao;
        this.payrollItemDao = payrollItemDao;
        this.scheduleShiftDao = scheduleShiftDao;
        this.attendanceRecordDao = attendanceRecordDao;
        this.jobDao = jobDao;
    }

    @Override
    public PayrollBatchVO createBatch(PayrollBatchCmd request) {
        PayrollBatch batch = new PayrollBatch();
        batch.setCompanyId(request.getCompanyId());
        batch.setName(request.getName());
        batch.setPeriodStart(request.getPeriodStart());
        batch.setPeriodEnd(request.getPeriodEnd());
        batch.setStatus("DRAFT");
        batch.setTotalAmount(BigDecimal.ZERO);
        batch.setWorkerCount(0);

        payrollBatchDao.save(batch);

        return toBatchResponse(batch);
    }

    @Override
    public PayrollBatchVO calculateBatch(Long batchId) {
        PayrollBatch batch = payrollBatchDao.findById(batchId)
                .orElseThrow(() -> new RuntimeException("Payroll batch not found: " + batchId));

        if (!"DRAFT".equals(batch.getStatus())) {
            throw new RuntimeException("Cannot calculate batch in status: " + batch.getStatus());
        }

        List<ScheduleShift> shifts = scheduleShiftDao.findByDateRange(
                batch.getPeriodStart(), batch.getPeriodEnd());

        Map<String, List<ScheduleShift>> grouped = shifts.stream()
                .collect(Collectors.groupingBy(s -> s.getWorkerId() + ":" + s.getJobId()));

        List<PayrollItem> items = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (Map.Entry<String, List<ScheduleShift>> entry : grouped.entrySet()) {
            List<ScheduleShift> workerShifts = entry.getValue();
            Long workerId = workerShifts.get(0).getWorkerId();
            Long jobId = workerShifts.get(0).getJobId();

            List<Long> shiftIds = workerShifts.stream()
                    .map(ScheduleShift::getId)
                    .collect(Collectors.toList());

            List<AttendanceRecord> attendanceRecords = attendanceRecordDao.findByShiftIds(shiftIds);

            BigDecimal totalHours = BigDecimal.ZERO;
            for (AttendanceRecord ar : attendanceRecords) {
                if (ar.getTotalHours() != null) {
                    totalHours = totalHours.add(ar.getTotalHours());
                }
            }

            List<JobRate> rates = jobDao.findRatesByJobId(jobId);
            if (rates.isEmpty()) {
                continue;
            }

            JobRate rate = rates.get(0);
            String rateType = rate.getType();
            BigDecimal rateAmount = rate.getAmount();
            BigDecimal totalPay;

            if ("HOURLY".equals(rateType)) {
                totalPay = totalHours.multiply(rateAmount).setScale(2, RoundingMode.HALF_UP);
            } else if ("DAILY".equals(rateType)) {
                long days = workerShifts.stream()
                        .map(ScheduleShift::getShiftDate)
                        .distinct()
                        .count();
                totalPay = BigDecimal.valueOf(days).multiply(rateAmount).setScale(2, RoundingMode.HALF_UP);
            } else {
                totalPay = BigDecimal.ZERO;
            }

            PayrollItem item = new PayrollItem();
            item.setBatchId(batchId);
            item.setWorkerId(workerId);
            item.setJobId(jobId);
            item.setTotalHours(totalHours);
            item.setRateType(rateType);
            item.setRateAmount(rateAmount);
            item.setTotalPay(totalPay);
            item.setStatus("PENDING");
            items.add(item);

            totalAmount = totalAmount.add(totalPay);
        }

        payrollItemDao.saveAll(items);

        batch.setStatus("CALCULATED");
        batch.setTotalAmount(totalAmount);
        batch.setWorkerCount(items.size());
        payrollBatchDao.update(batch);

        return toBatchResponse(batch);
    }

    @Override
    public PayrollBatchVO confirmBatch(Long batchId) {
        PayrollBatch batch = payrollBatchDao.findById(batchId)
                .orElseThrow(() -> new RuntimeException("Payroll batch not found: " + batchId));

        if (!"CALCULATED".equals(batch.getStatus())) {
            throw new RuntimeException("Cannot confirm batch in status: " + batch.getStatus());
        }

        payrollBatchDao.updateStatus(batchId, "CONFIRMED");
        batch.setStatus("CONFIRMED");

        return toBatchResponse(batch);
    }

    @Override
    public PayrollBatchVO payBatch(Long batchId) {
        PayrollBatch batch = payrollBatchDao.findById(batchId)
                .orElseThrow(() -> new RuntimeException("Payroll batch not found: " + batchId));

        if (!"CONFIRMED".equals(batch.getStatus())) {
            throw new RuntimeException("Cannot pay batch in status: " + batch.getStatus());
        }

        payrollBatchDao.updateStatus(batchId, "PAID");
        batch.setStatus("PAID");

        return toBatchResponse(batch);
    }

    @Override
    public PayrollBatchVO getBatchById(Long batchId) {
        PayrollBatch batch = payrollBatchDao.findById(batchId)
                .orElseThrow(() -> new RuntimeException("Payroll batch not found: " + batchId));
        return toBatchResponse(batch);
    }

    @Override
    public List<PayrollBatchVO> getBatchesByCompany(Long companyId) {
        return payrollBatchDao.findByCompanyId(companyId)
                .stream()
                .map(this::toBatchResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<PayrollItemVO> getBatchItems(Long batchId) {
        payrollBatchDao.findById(batchId)
                .orElseThrow(() -> new RuntimeException("Payroll batch not found: " + batchId));

        return payrollItemDao.findByBatchId(batchId)
                .stream()
                .map(this::toItemResponse)
                .collect(Collectors.toList());
    }

    private PayrollBatchVO toBatchResponse(PayrollBatch batch) {
        PayrollBatchVO response = new PayrollBatchVO();
        response.setId(batch.getId());
        response.setCompanyId(batch.getCompanyId());
        response.setName(batch.getName());
        response.setPeriodStart(batch.getPeriodStart());
        response.setPeriodEnd(batch.getPeriodEnd());
        response.setStatus(batch.getStatus());
        response.setTotalAmount(batch.getTotalAmount());
        response.setWorkerCount(batch.getWorkerCount());
        response.setCreatedAt(batch.getCreatedAt());
        response.setUpdatedAt(batch.getUpdatedAt());
        return response;
    }

    private PayrollItemVO toItemResponse(PayrollItem item) {
        PayrollItemVO response = new PayrollItemVO();
        response.setId(item.getId());
        response.setBatchId(item.getBatchId());
        response.setWorkerId(item.getWorkerId());
        response.setJobId(item.getJobId());
        response.setTotalHours(item.getTotalHours());
        response.setRateType(item.getRateType());
        response.setRateAmount(item.getRateAmount());
        response.setTotalPay(item.getTotalPay());
        response.setStatus(item.getStatus());
        response.setCreatedAt(item.getCreatedAt());
        response.setUpdatedAt(item.getUpdatedAt());
        return response;
    }
}
