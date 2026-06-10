package com.parttime.enterprise.service.impl;

import com.parttime.enterprise.enums.ShiftStatus;
import com.parttime.enterprise.mapper.AttendanceRecordMapper;
import com.parttime.enterprise.mapper.CompanyWorkerMapper;
import com.parttime.enterprise.mapper.JobMapper;
import com.parttime.enterprise.mapper.ScheduleShiftMapper;
import com.parttime.enterprise.mapper.WorkerNotificationMapper;
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
    @Resource
    private WorkerNotificationMapper workerNotificationMapper;

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
            workerNotificationMapper.insertWorkerNotification(shift.getWorkerId(), "SCHEDULE_ASSIGNED", "schedule",
                    "排班已生成", "您有新的排班，请及时查看", "SHIFT", shift.getId());
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

        List<Long> jobIds = shifts.stream().map(ScheduleShift::getJobId).distinct().toList();
        List<Long> workerIds = shifts.stream().map(ScheduleShift::getWorkerId).filter(Objects::nonNull).distinct().toList();
        Map<Long, Job> jobMap = new HashMap<>();
        Map<Long, String> workerNameMap = new HashMap<>();
        Map<Long, Integer> workerAgeMap = new HashMap<>();
        if (!jobIds.isEmpty()) {
            jobMapper.findByIds(jobIds).forEach(j -> jobMap.put(j.getId(), j));
        }
        if (!workerIds.isEmpty()) {
            workerSyncMapper.findWorkerNamesByIds(workerIds).forEach(m -> workerNameMap.put((Long) m.get("id"), (String) m.get("name")));
            workerSyncMapper.findWorkerBirthdaysByIds(workerIds).forEach(m -> {
                Long wid = (Long) m.get("worker_id");
                java.time.LocalDate bd = (java.time.LocalDate) m.get("birthday");
                if (bd != null) workerAgeMap.put(wid, java.time.LocalDate.now().getYear() - bd.getYear());
            });
        }

        Map<Long, Job> finalJobMap = jobMap;
        Map<Long, String> finalWorkerNameMap = workerNameMap;
        Map<Long, Integer> finalWorkerAgeMap = workerAgeMap;
        List<ScheduleShiftVO> voList = shifts.stream()
                .map(s -> toShiftResponse(s, recordMap.get(s.getId()), finalJobMap, finalWorkerNameMap, finalWorkerAgeMap))
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
        if (shift.getWorkerId() != null) {
            workerNotificationMapper.insertWorkerNotification(shift.getWorkerId(), "SCHEDULE_UPDATED", "schedule",
                    "排班已变更", "您的排班信息已变更，请及时查看", "SHIFT", shift.getId());
        }
        return toShiftResponse(shift);
    }

    @Override
    public void removeShift(Long id) {
        ScheduleShift shift = shiftMapper.findById(id)
                .orElseThrow(() -> new RuntimeException("ScheduleShift not found: " + id));
        shiftMapper.cancelShift(id);
        if (shift.getWorkerId() != null) {
            workerNotificationMapper.insertWorkerNotification(shift.getWorkerId(), "SCHEDULE_CANCELLED", "schedule",
                    "排班已取消", "您的排班已取消，请及时查看", "SHIFT", shift.getId());
        }
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

        List<Long> workerIds = shifts.stream().map(ScheduleShift::getWorkerId).filter(Objects::nonNull).distinct().toList();
        Map<Long, String> workerNameMap = new HashMap<>();
        Map<Long, Integer> workerAgeMap = new HashMap<>();
        if (!workerIds.isEmpty()) {
            workerSyncMapper.findWorkerNamesByIds(workerIds).forEach(m -> workerNameMap.put((Long) m.get("id"), (String) m.get("name")));
            workerSyncMapper.findWorkerBirthdaysByIds(workerIds).forEach(m -> {
                Long wid = (Long) m.get("worker_id");
                java.time.LocalDate bd = (java.time.LocalDate) m.get("birthday");
                if (bd != null) workerAgeMap.put(wid, java.time.LocalDate.now().getYear() - bd.getYear());
            });
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
            String workerName = workerNameMap.get(shift.getWorkerId());
            report.setWorkerName(workerName != null ? workerName : workerSyncMapper.findWorkerNameById(shift.getWorkerId()));
            Integer workerAge = workerAgeMap.get(shift.getWorkerId());
            if (workerAge != null) {
                report.setWorkerAge(workerAge);
            } else {
                java.time.LocalDate birthday = workerSyncMapper.findWorkerBirthdayById(shift.getWorkerId());
                if (birthday != null) {
                    report.setWorkerAge(java.time.LocalDate.now().getYear() - birthday.getYear());
                }
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
        return toShiftResponse(shift, null, new HashMap<>(), new HashMap<>(), new HashMap<>());
    }

    private ScheduleShiftVO toShiftResponse(ScheduleShift shift, AttendanceRecord record) {
        return toShiftResponse(shift, record, new HashMap<>(), new HashMap<>(), new HashMap<>());
    }

    private ScheduleShiftVO toShiftResponse(ScheduleShift shift, AttendanceRecord record,
                                             Map<Long, Job> jobMap, Map<Long, String> workerNameMap, Map<Long, Integer> workerAgeMap) {
        ScheduleShiftVO response = new ScheduleShiftVO();
        response.setId(shift.getId());
        response.setJobId(shift.getJobId());
        Job job = jobMap.get(shift.getJobId());
        response.setJobTitle(job != null ? job.getTitle() : jobMapper.findById(shift.getJobId()).map(Job::getTitle).orElse(null));
        String workerName = workerNameMap.get(shift.getWorkerId());
        response.setWorkerName(workerName != null ? workerName : workerSyncMapper.findWorkerNameById(shift.getWorkerId()));
        Integer workerAge = workerAgeMap.get(shift.getWorkerId());
        if (workerAge != null) {
            response.setWorkerAge(workerAge);
        } else {
            java.time.LocalDate birthday = workerSyncMapper.findWorkerBirthdayById(shift.getWorkerId());
            if (birthday != null) {
                response.setWorkerAge(java.time.LocalDate.now().getYear() - birthday.getYear());
            }
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
