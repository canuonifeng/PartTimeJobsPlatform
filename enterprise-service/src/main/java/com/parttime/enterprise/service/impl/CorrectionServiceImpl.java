package com.parttime.enterprise.service.impl;

import com.parttime.enterprise.enums.CorrectionStatus;
import com.parttime.enterprise.enums.ShiftStatus;
import com.parttime.enterprise.mapper.AttendanceCorrectionMapper;
import com.parttime.enterprise.mapper.AttendanceRecordMapper;
import com.parttime.enterprise.mapper.ScheduleShiftMapper;
import com.parttime.enterprise.mapper.WorkerSyncMapper;
import com.parttime.enterprise.mapper.JobMapper;
import com.parttime.enterprise.pojo.entity.AttendanceCorrection;
import com.parttime.enterprise.pojo.entity.AttendanceRecord;
import com.parttime.enterprise.pojo.entity.ScheduleShift;
import com.parttime.enterprise.pojo.entity.Job;
import com.parttime.enterprise.pojo.vo.CorrectionVO;
import com.parttime.enterprise.pojo.vo.PageVO;
import com.parttime.enterprise.service.CorrectionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CorrectionServiceImpl implements CorrectionService {

    @Resource
    private AttendanceCorrectionMapper correctionMapper;
    @Resource
    private AttendanceRecordMapper attendanceRecordMapper;
    @Resource
    private ScheduleShiftMapper shiftMapper;
    @Resource
    private WorkerSyncMapper workerSyncMapper;
    @Resource
    private JobMapper jobMapper;

    public void setCorrectionMapper(AttendanceCorrectionMapper m) { this.correctionMapper = m; }
    public void setAttendanceRecordMapper(AttendanceRecordMapper m) { this.attendanceRecordMapper = m; }
    public void setShiftMapper(ScheduleShiftMapper m) { this.shiftMapper = m; }
    public void setWorkerSyncMapper(WorkerSyncMapper m) { this.workerSyncMapper = m; }
    public void setJobMapper(JobMapper m) { this.jobMapper = m; }

    @Override
    public PageVO<CorrectionVO> listCorrections(String status, String keyword,
                                                 String dateFrom, String dateTo,
                                                 Integer page, Integer pageSize) {
        int offset = (page != null && page > 0) ? (page - 1) * pageSize : 0;
        int limit = pageSize != null ? pageSize : 20;

        List<AttendanceCorrection> list = correctionMapper.search(status, keyword, dateFrom, dateTo, offset, limit);
        int total = correctionMapper.countSearch(status, keyword, dateFrom, dateTo);

        List<Long> shiftIds = list.stream().map(AttendanceCorrection::getShiftId).collect(Collectors.toList());
        Map<Long, ScheduleShift> shiftMap = new HashMap<>();
        if (!shiftIds.isEmpty()) {
            List<ScheduleShift> shifts = shiftMapper.findByIds(shiftIds);
            if (shifts != null) shifts.forEach(s -> shiftMap.put(s.getId(), s));
        }

        List<CorrectionVO> voList = list.stream().map(c -> {
            CorrectionVO vo = new CorrectionVO();
            vo.setId(c.getId());
            vo.setShiftId(c.getShiftId());
            vo.setWorkerId(c.getWorkerId());
            vo.setReason(c.getReason());
            vo.setStatus(c.getStatus());
            vo.setRejectReason(c.getRejectReason());
            vo.setCreatedAt(c.getCreatedAt());
            vo.setProcessedAt(c.getProcessedAt());

            ScheduleShift shift = shiftMap.get(c.getShiftId());
            if (shift != null) {
                vo.setJobId(shift.getJobId());
                vo.setShiftDate(shift.getShiftDate());
                vo.setStartTime(shift.getStartTime());
                vo.setEndTime(shift.getEndTime());
                vo.setWorkerName(workerSyncMapper.findWorkerNameById(shift.getWorkerId()));
                vo.setJobTitle(jobMapper.findById(shift.getJobId()).map(Job::getTitle).orElse(null));
            }
            return vo;
        }).collect(Collectors.toList());

        return new PageVO<>(voList, total);
    }

    @Override
    @Transactional
    public void approve(Long id, Long processorId) {
        AttendanceCorrection correction = correctionMapper.findById(id)
                .orElseThrow(() -> new RuntimeException("Correction not found: " + id));

        if (!CorrectionStatus.PENDING.name().equals(correction.getStatus())) {
            throw new RuntimeException("Correction is not in PENDING status");
        }

        ScheduleShift shift = shiftMapper.findById(correction.getShiftId())
                .orElseThrow(() -> new RuntimeException("ScheduleShift not found: " + correction.getShiftId()));
        Job job = jobMapper.findById(shift.getJobId())
                .orElseThrow(() -> new RuntimeException("Job not found: " + shift.getJobId()));

        Optional<AttendanceRecord> existing = attendanceRecordMapper.findByShiftId(correction.getShiftId());

        LocalDateTime checkIn = LocalDateTime.of(shift.getShiftDate(), shift.getStartTime());
        LocalDateTime checkOut = LocalDateTime.of(shift.getShiftDate(), shift.getEndTime());
        Duration duration = Duration.between(shift.getStartTime(), shift.getEndTime());
        BigDecimal totalHours = BigDecimal.valueOf(duration.toMinutes() / 60.0)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal scheduledPay = BigDecimal.ZERO;
        String rateType = shift.getSalaryType();
        BigDecimal rateAmount = shift.getSalaryAmount();
        if (rateAmount != null && rateAmount.compareTo(BigDecimal.ZERO) > 0) {
            if ("HOURLY".equals(rateType)) {
                scheduledPay = totalHours.multiply(rateAmount).setScale(2, RoundingMode.HALF_UP);
            } else {
                scheduledPay = rateAmount.setScale(2, RoundingMode.HALF_UP);
            }
        }

        if (existing.isPresent()) {
            AttendanceRecord record = existing.get();
            record.setJobId(shift.getJobId());
            record.setCompanyId(job.getCompanyId());
            record.setCheckInTime(checkIn);
            record.setCheckOutTime(checkOut);
            record.setTotalHours(totalHours);
            record.setScheduledPay(scheduledPay);
            record.setRemark("补卡");
            attendanceRecordMapper.update(record);
        } else {
            AttendanceRecord record = new AttendanceRecord();
            record.setShiftId(correction.getShiftId());
            record.setJobId(shift.getJobId());
            record.setCompanyId(job.getCompanyId());
            record.setWorkerId(correction.getWorkerId());
            record.setCheckInTime(checkIn);
            record.setCheckOutTime(checkOut);
            record.setTotalHours(totalHours);
            record.setScheduledPay(scheduledPay);
            record.setStatus(ShiftStatus.ON_DUTY.name());
            record.setRemark("补卡");
            attendanceRecordMapper.insert(record);
        }

        if (ShiftStatus.SCHEDULED.name().equals(shift.getStatus())) {
            shift.setStatus(ShiftStatus.ON_DUTY.name());
            shift.setUpdatedAt(LocalDateTime.now());
            shiftMapper.update(shift);
        }

        correction.setStatus(CorrectionStatus.APPROVED.name());
        correction.setProcessedAt(LocalDateTime.now());
        correction.setProcessorId(processorId);
        correctionMapper.update(correction);
    }

    @Override
    @Transactional
    public void reject(Long id, Long processorId, String rejectReason) {
        AttendanceCorrection correction = correctionMapper.findById(id)
                .orElseThrow(() -> new RuntimeException("Correction not found: " + id));

        if (!CorrectionStatus.PENDING.name().equals(correction.getStatus())) {
            throw new RuntimeException("Correction is not in PENDING status");
        }

        correction.setStatus(CorrectionStatus.REJECTED.name());
        correction.setRejectReason(rejectReason);
        correction.setProcessedAt(LocalDateTime.now());
        correction.setProcessorId(processorId);
        correctionMapper.update(correction);
    }
}
