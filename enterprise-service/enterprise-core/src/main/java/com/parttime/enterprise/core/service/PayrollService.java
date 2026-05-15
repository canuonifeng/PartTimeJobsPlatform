package com.parttime.enterprise.core.service;

import com.parttime.enterprise.api.dto.PayrollBatchRequest;
import com.parttime.enterprise.api.dto.PayrollBatchResponse;
import com.parttime.enterprise.api.dto.PayrollItemResponse;
import com.parttime.enterprise.core.domain.AttendanceRecord;
import com.parttime.enterprise.core.domain.JobRate;
import com.parttime.enterprise.core.domain.PayrollBatch;
import com.parttime.enterprise.core.domain.PayrollItem;
import com.parttime.enterprise.core.domain.ScheduleShift;
import com.parttime.enterprise.core.repository.AttendanceRecordRepository;
import com.parttime.enterprise.core.repository.JobRepository;
import com.parttime.enterprise.core.repository.PayrollBatchRepository;
import com.parttime.enterprise.core.repository.PayrollItemRepository;
import com.parttime.enterprise.core.repository.ScheduleShiftRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PayrollService {

    private final PayrollBatchRepository payrollBatchRepository;
    private final PayrollItemRepository payrollItemRepository;
    private final ScheduleShiftRepository scheduleShiftRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final JobRepository jobRepository;

    public PayrollService(PayrollBatchRepository payrollBatchRepository,
                          PayrollItemRepository payrollItemRepository,
                          ScheduleShiftRepository scheduleShiftRepository,
                          AttendanceRecordRepository attendanceRecordRepository,
                          JobRepository jobRepository) {
        this.payrollBatchRepository = payrollBatchRepository;
        this.payrollItemRepository = payrollItemRepository;
        this.scheduleShiftRepository = scheduleShiftRepository;
        this.attendanceRecordRepository = attendanceRecordRepository;
        this.jobRepository = jobRepository;
    }

    public PayrollBatchResponse createBatch(PayrollBatchRequest request) {
        PayrollBatch batch = new PayrollBatch();
        batch.setCompanyId(request.getCompanyId());
        batch.setName(request.getName());
        batch.setPeriodStart(request.getPeriodStart());
        batch.setPeriodEnd(request.getPeriodEnd());
        batch.setStatus("DRAFT");
        batch.setTotalAmount(BigDecimal.ZERO);
        batch.setWorkerCount(0);

        payrollBatchRepository.save(batch);

        return toBatchResponse(batch);
    }

    public PayrollBatchResponse calculateBatch(Long batchId) {
        PayrollBatch batch = payrollBatchRepository.findById(batchId)
                .orElseThrow(() -> new RuntimeException("Payroll batch not found: " + batchId));

        if (!"DRAFT".equals(batch.getStatus())) {
            throw new RuntimeException("Cannot calculate batch in status: " + batch.getStatus());
        }

        List<ScheduleShift> shifts = scheduleShiftRepository.findByDateRange(
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

            List<AttendanceRecord> attendanceRecords = attendanceRecordRepository.findByShiftIds(shiftIds);

            BigDecimal totalHours = BigDecimal.ZERO;
            for (AttendanceRecord ar : attendanceRecords) {
                if (ar.getTotalHours() != null) {
                    totalHours = totalHours.add(ar.getTotalHours());
                }
            }

            List<JobRate> rates = jobRepository.findRatesByJobId(jobId);
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

        payrollItemRepository.saveAll(items);

        batch.setStatus("CALCULATED");
        batch.setTotalAmount(totalAmount);
        batch.setWorkerCount(items.size());
        payrollBatchRepository.update(batch);

        return toBatchResponse(batch);
    }

    public PayrollBatchResponse confirmBatch(Long batchId) {
        PayrollBatch batch = payrollBatchRepository.findById(batchId)
                .orElseThrow(() -> new RuntimeException("Payroll batch not found: " + batchId));

        if (!"CALCULATED".equals(batch.getStatus())) {
            throw new RuntimeException("Cannot confirm batch in status: " + batch.getStatus());
        }

        payrollBatchRepository.updateStatus(batchId, "CONFIRMED");
        batch.setStatus("CONFIRMED");

        return toBatchResponse(batch);
    }

    public PayrollBatchResponse payBatch(Long batchId) {
        PayrollBatch batch = payrollBatchRepository.findById(batchId)
                .orElseThrow(() -> new RuntimeException("Payroll batch not found: " + batchId));

        if (!"CONFIRMED".equals(batch.getStatus())) {
            throw new RuntimeException("Cannot pay batch in status: " + batch.getStatus());
        }

        payrollBatchRepository.updateStatus(batchId, "PAID");
        batch.setStatus("PAID");

        return toBatchResponse(batch);
    }

    public PayrollBatchResponse getBatchById(Long batchId) {
        PayrollBatch batch = payrollBatchRepository.findById(batchId)
                .orElseThrow(() -> new RuntimeException("Payroll batch not found: " + batchId));
        return toBatchResponse(batch);
    }

    public List<PayrollBatchResponse> getBatchesByCompany(Long companyId) {
        return payrollBatchRepository.findByCompanyId(companyId)
                .stream()
                .map(this::toBatchResponse)
                .collect(Collectors.toList());
    }

    public List<PayrollItemResponse> getBatchItems(Long batchId) {
        payrollBatchRepository.findById(batchId)
                .orElseThrow(() -> new RuntimeException("Payroll batch not found: " + batchId));

        return payrollItemRepository.findByBatchId(batchId)
                .stream()
                .map(this::toItemResponse)
                .collect(Collectors.toList());
    }

    private PayrollBatchResponse toBatchResponse(PayrollBatch batch) {
        PayrollBatchResponse response = new PayrollBatchResponse();
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

    private PayrollItemResponse toItemResponse(PayrollItem item) {
        PayrollItemResponse response = new PayrollItemResponse();
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
