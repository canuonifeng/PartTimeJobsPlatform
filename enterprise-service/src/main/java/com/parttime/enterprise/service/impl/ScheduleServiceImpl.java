package com.parttime.enterprise.service.impl;

import com.parttime.enterprise.enums.ShiftStatus;
import com.parttime.enterprise.mapper.AttendanceRecordMapper;
import com.parttime.enterprise.mapper.CompanyWorkerMapper;
import com.parttime.enterprise.mapper.JobMapper;
import com.parttime.enterprise.mapper.ScheduleShiftMapper;
import com.parttime.enterprise.mapper.WorkerSyncMapper;
import com.parttime.enterprise.pojo.cmd.ScheduleShiftCmd;
import com.parttime.enterprise.pojo.entity.AttendanceRecord;
import com.parttime.enterprise.pojo.entity.Job;
import com.parttime.enterprise.pojo.entity.ScheduleShift;
import com.parttime.enterprise.pojo.vo.AttendanceReportVO;
import com.parttime.enterprise.pojo.vo.PageVO;
import com.parttime.enterprise.pojo.vo.ScheduleShiftVO;
import com.parttime.enterprise.service.ScheduleService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ScheduleServiceImpl implements ScheduleService {

    @Resource
    private ScheduleShiftMapper shiftMapper;
    @Resource
    private AttendanceRecordMapper attendanceRecordMapper;
    @Resource
    private JobMapper jobMapper;
    @Resource
    private CompanyWorkerMapper companyWorkerMapper;
    @Resource
    private WorkerSyncMapper workerSyncMapper;

    @Override
    public ScheduleShiftVO assignShift(ScheduleShiftCmd request) {
        Job job = jobMapper.findById(request.getJobId())
                .orElseThrow(() -> new RuntimeException("Job not found: " + request.getJobId()));
        ScheduleShift shift = new ScheduleShift();
        shift.setJobId(request.getJobId());
        shift.setCompanyId(job.getCompanyId());
        shift.setWorkerId(request.getWorkerId());
        shift.setShiftDate(request.getShiftDate());
        shift.setStartTime(request.getStartTime());
        shift.setEndTime(request.getEndTime());
        shift.setLocationLat(request.getLocationLat());
        shift.setLocationLng(request.getLocationLng());
        shift.setLocationRadius(request.getLocationRadius());
        shift.setLocationName(request.getLocationName());
        shift.setStatus(ShiftStatus.SCHEDULED.name());
        shiftMapper.insert(shift);
        Long shiftId = shift.getId();
        shift = shiftMapper.findById(shiftId)
                .orElseThrow(() -> new RuntimeException("ScheduleShift not found: " + shiftId));
        if (shift.getWorkerId() != null) {
            companyWorkerMapper.upsert(job.getCompanyId(), shift.getWorkerId());
        }
        return toShiftResponse(shift);
    }

    @Override
    public PageVO<ScheduleShiftVO> getShifts(Long jobId, Long workerId, LocalDate shiftDate, Integer page, Integer pageSize) {
        List<ScheduleShift> shifts;
        if (jobId != null && shiftDate != null) {
            shifts = shiftMapper.findByJobIdAndDate(jobId, shiftDate);
        } else if (jobId != null) {
            shifts = shiftMapper.findByJobId(jobId);
        } else if (workerId != null) {
            shifts = shiftMapper.findByWorkerId(workerId);
        } else {
            shifts = shiftMapper.findAll();
        }

        int total = shifts.size();
        if (page != null && pageSize != null && pageSize > 0) {
            int fromIndex = Math.max(page - 1, 0) * pageSize;
            if (fromIndex >= total) {
                shifts = List.of();
            } else {
                int toIndex = Math.min(fromIndex + pageSize, total);
                shifts = shifts.subList(fromIndex, toIndex);
            }
        }

        List<Long> shiftIds = shifts.stream().map(ScheduleShift::getId).collect(Collectors.toList());
        Map<Long, AttendanceRecord> recordMap = new HashMap<>();
        if (!shiftIds.isEmpty()) {
            attendanceRecordMapper.findByShiftIds(shiftIds)
                    .forEach(r -> recordMap.put(r.getShiftId(), r));
        }

        List<ScheduleShiftVO> voList = shifts.stream()
                .map(s -> toShiftResponse(s, recordMap.get(s.getId())))
                .collect(Collectors.toList());
        return new PageVO<>(voList, total);
    }

    @Override
    public ScheduleShiftVO updateShift(Long id, ScheduleShiftCmd request) {
        ScheduleShift shift = shiftMapper.findById(id)
                .orElseThrow(() -> new RuntimeException("ScheduleShift not found: " + id));
        shift.setJobId(request.getJobId());
        shift.setWorkerId(request.getWorkerId());
        shift.setShiftDate(request.getShiftDate());
        shift.setStartTime(request.getStartTime());
        shift.setEndTime(request.getEndTime());
        shift.setLocationLat(request.getLocationLat());
        shift.setLocationLng(request.getLocationLng());
        shift.setLocationRadius(request.getLocationRadius());
        shift.setLocationName(request.getLocationName());
        shiftMapper.update(shift);
        shift = shiftMapper.findById(id)
                .orElseThrow(() -> new RuntimeException("ScheduleShift not found: " + id));
        return toShiftResponse(shift);
    }

    @Override
    public void removeShift(Long id) {
        shiftMapper.cancelShift(id);
    }

    @Override
    public List<AttendanceReportVO> getAttendanceReport(Long jobId, Long shiftId, LocalDate date) {
        List<ScheduleShift> shifts;
        if (shiftId != null) {
            ScheduleShift shift = shiftMapper.findById(shiftId)
                    .orElseThrow(() -> new RuntimeException("ScheduleShift not found: " + shiftId));
            shifts = List.of(shift);
        } else if (jobId != null && date != null) {
            shifts = shiftMapper.findByJobIdAndDate(jobId, date);
        } else if (jobId != null) {
            shifts = shiftMapper.findByJobId(jobId);
        } else {
            throw new RuntimeException("Either jobId or shiftId is required");
        }

        List<Long> shiftIds = shifts.stream().map(ScheduleShift::getId).collect(Collectors.toList());
        Map<Long, AttendanceRecord> recordMap = new HashMap<>();
        if (!shiftIds.isEmpty()) {
            List<AttendanceRecord> records = attendanceRecordMapper.findByShiftIds(shiftIds);
            for (AttendanceRecord record : records) {
                recordMap.put(record.getShiftId(), record);
            }
        }

        return shifts.stream().map(shift -> {
            AttendanceReportVO report = new AttendanceReportVO();
            report.setShiftId(shift.getId());
            report.setJobId(shift.getJobId());
            report.setWorkerId(shift.getWorkerId());
            report.setShiftDate(shift.getShiftDate());
            report.setStartTime(shift.getStartTime());
            report.setEndTime(shift.getEndTime());
            report.setShiftStatus(shift.getStatus());
            report.setWorkerName(workerSyncMapper.findWorkerNameById(shift.getWorkerId()));
            java.time.LocalDate birthday = workerSyncMapper.findWorkerBirthdayById(shift.getWorkerId());
            if (birthday != null) {
                report.setWorkerAge(java.time.LocalDate.now().getYear() - birthday.getYear());
            }

            AttendanceRecord record = recordMap.get(shift.getId());
            if (record != null) {
                report.setCheckInTime(record.getCheckInTime());
                report.setCheckOutTime(record.getCheckOutTime());
                report.setTotalHours(record.getTotalHours());
                report.setAttendanceStatus(record.getStatus());
            } else {
                report.setAttendanceStatus("NO_RECORD");
            }

            return report;
        }).collect(Collectors.toList());
    }

    private ScheduleShiftVO toShiftResponse(ScheduleShift shift) {
        return toShiftResponse(shift, null);
    }

    private ScheduleShiftVO toShiftResponse(ScheduleShift shift, AttendanceRecord record) {
        ScheduleShiftVO response = new ScheduleShiftVO();
        response.setId(shift.getId());
        response.setJobId(shift.getJobId());
        response.setJobTitle(jobMapper.findById(shift.getJobId()).map(Job::getTitle).orElse(null));
        response.setWorkerName(workerSyncMapper.findWorkerNameById(shift.getWorkerId()));
        java.time.LocalDate birthday = workerSyncMapper.findWorkerBirthdayById(shift.getWorkerId());
        if (birthday != null) {
            response.setWorkerAge(java.time.LocalDate.now().getYear() - birthday.getYear());
        }
        response.setApplicationId(shift.getApplicationId());
        response.setSalaryType(shift.getSalaryType());
        response.setSalaryAmount(shift.getSalaryAmount());
        response.setSalaryCurrency(shift.getSalaryCurrency());
        response.setWorkerId(shift.getWorkerId());
        response.setShiftDate(shift.getShiftDate());
        response.setStartTime(shift.getStartTime());
        response.setEndTime(shift.getEndTime());
        response.setLocationLat(shift.getLocationLat());
        response.setLocationLng(shift.getLocationLng());
        response.setLocationRadius(shift.getLocationRadius());
        response.setLocationName(shift.getLocationName());
        response.setStatus(shift.getStatus());
        response.setCreatedAt(shift.getCreatedAt());
        response.setUpdatedAt(shift.getUpdatedAt());

        if (record != null) {
            response.setCheckInTime(record.getCheckInTime());
            response.setCheckOutTime(record.getCheckOutTime());
            if (record.getCheckOutTime() != null) {
                response.setAttendanceStatus("CHECKED_OUT");
            } else if (record.getCheckInTime() != null) {
                response.setAttendanceStatus("CHECKED_IN");
            } else {
                response.setAttendanceStatus("NO_CHECK_IN");
            }
        } else {
            response.setAttendanceStatus("NO_CHECK_IN");
        }

        return response;
    }
}
