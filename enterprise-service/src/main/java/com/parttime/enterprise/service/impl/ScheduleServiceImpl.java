package com.parttime.enterprise.service.impl;

import com.parttime.enterprise.enums.ShiftStatus;
import com.parttime.enterprise.mapper.AttendanceRecordMapper;
import com.parttime.enterprise.mapper.CompanyWorkerMapper;
import com.parttime.enterprise.mapper.JobMapper;
import com.parttime.enterprise.mapper.JobRateMapper;
import com.parttime.enterprise.mapper.JobScheduleMapper;
import com.parttime.enterprise.mapper.ScheduleApplicationMapper;
import com.parttime.enterprise.mapper.ScheduleShiftMapper;
import com.parttime.enterprise.mapper.WorkerNotificationMapper;
import com.parttime.enterprise.mapper.WorkerSyncMapper;
import com.parttime.enterprise.pojo.cmd.ScheduleBatchCreateCmd;
import com.parttime.enterprise.pojo.cmd.ScheduleCopyCmd;
import com.parttime.enterprise.pojo.cmd.ScheduleExportCmd;
import com.parttime.enterprise.pojo.cmd.ScheduleManageUpdateCmd;
import com.parttime.enterprise.pojo.cmd.ScheduleShiftCmd;
import com.parttime.enterprise.pojo.entity.AttendanceRecord;
import com.parttime.enterprise.pojo.entity.Job;
import com.parttime.enterprise.pojo.entity.JobRate;
import com.parttime.enterprise.pojo.entity.JobSchedule;
import com.parttime.enterprise.pojo.entity.ScheduleShift;
import com.parttime.enterprise.pojo.vo.AttendanceReportVO;
import com.parttime.enterprise.pojo.vo.PageVO;
import com.parttime.enterprise.pojo.vo.ScheduleApplicantVO;
import com.parttime.enterprise.pojo.vo.ScheduleExportVO;
import com.parttime.enterprise.pojo.vo.ScheduleManagementVO;
import com.parttime.enterprise.pojo.vo.ScheduleShiftVO;
import com.parttime.enterprise.service.ScheduleService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private JobRateMapper jobRateMapper;
    @Resource
    private JobScheduleMapper jobScheduleMapper;
    @Resource
    private ScheduleApplicationMapper scheduleApplicationMapper;
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
        JobRate rate = jobRateMapper.findByJobId(request.getJobId()).stream()
                .findFirst().orElse(null);
        if (rate != null) {
            shift.setSalaryType(rate.getType());
            shift.setSalaryAmount(rate.getAmount());
            shift.setSalaryCurrency(rate.getCurrency());
        }
        shift.setStatus(ShiftStatus.SCHEDULED.name());
        shiftMapper.insert(shift);
        Long shiftId = shift.getId();
        shift = shiftMapper.findById(shiftId)
                .orElseThrow(() -> new RuntimeException("ScheduleShift not found: " + shiftId));
        if (shift.getWorkerId() != null) {
            companyWorkerMapper.upsert(job.getCompanyId(), shift.getWorkerId());
            String shiftInfo = job.getTitle() + " " + shift.getShiftDate() + " " + shift.getStartTime() + "-" + shift.getEndTime();
            workerNotificationMapper.insertWorkerNotification(shift.getWorkerId(), "SCHEDULE_ASSIGNED", "schedule",
                    "排班已生成", "您有新的排班：" + shiftInfo, "SHIFT", shift.getId());
        }
        return toShiftResponse(shift);
    }

    @Override
    public PageVO<ScheduleShiftVO> getShifts(Long companyId, Long jobId, Long workerId, LocalDate shiftDate, String status, Integer page, Integer pageSize) {
        String normalizedStatus = status == null || status.isBlank() ? null : status.trim().toUpperCase();
        List<ScheduleShift> shifts;
        int total;
        if (page != null && pageSize != null && pageSize > 0) {
            int currentPage = Math.max(page, 1);
            int offset = (currentPage - 1) * pageSize;
            if (normalizedStatus != null) {
                shifts = shiftMapper.findPageByStatus(companyId, jobId, workerId, shiftDate, normalizedStatus, offset, pageSize);
                total = shiftMapper.countPageByStatus(companyId, jobId, workerId, shiftDate, normalizedStatus);
            } else {
                shifts = shiftMapper.findPage(companyId, jobId, workerId, shiftDate, offset, pageSize);
                total = shiftMapper.countPage(companyId, jobId, workerId, shiftDate);
            }
        } else if (jobId != null && shiftDate != null) {
            shifts = shiftMapper.findByJobIdAndDate(jobId, shiftDate);
            total = shifts.size();
        } else if (jobId != null) {
            shifts = shiftMapper.findByJobId(jobId);
            total = shifts.size();
        } else if (workerId != null) {
            shifts = shiftMapper.findByWorkerId(workerId);
            total = shifts.size();
        } else if (companyId != null) {
            shifts = shiftMapper.findByCompanyId(companyId);
            total = shifts.size();
        } else {
            shifts = shiftMapper.findAll();
            total = shifts.size();
        }
        if (normalizedStatus != null && (page == null || pageSize == null || pageSize <= 0)) {
            shifts = shifts.stream().filter(s -> normalizedStatus.equals(s.getStatus())).collect(Collectors.toList());
            total = shifts.size();
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
        Map<Long, String> workerPhoneMap = new HashMap<>();
        Map<Long, String> workerGenderMap = new HashMap<>();
        Map<Long, Integer> workerAgeMap = new HashMap<>();
        if (!jobIds.isEmpty()) {
            jobMapper.findByIds(jobIds).forEach(j -> jobMap.put(j.getId(), j));
        }
        if (!workerIds.isEmpty()) {
            workerSyncMapper.findWorkerNamesByIds(workerIds).forEach(m -> workerNameMap.put((Long) m.get("id"), (String) m.get("name")));
            workerSyncMapper.findWorkerPhonesByIds(workerIds).forEach(m -> workerPhoneMap.put((Long) m.get("id"), (String) m.get("phone")));
            workerSyncMapper.findWorkerGendersByIds(workerIds).forEach(m -> workerGenderMap.put((Long) m.get("worker_id"), (String) m.get("gender")));
            workerSyncMapper.findWorkerBirthdaysByIds(workerIds).forEach(m -> {
                Long wid = (Long) m.get("worker_id");
                Object bdRaw = m.get("birthday");
                java.time.LocalDate bd = bdRaw instanceof java.sql.Date sd ? sd.toLocalDate()
                        : bdRaw instanceof java.time.LocalDate ld ? ld : null;
                if (bd != null) workerAgeMap.put(wid, java.time.LocalDate.now().getYear() - bd.getYear());
            });
        }

        Map<Long, Job> finalJobMap = jobMap;
        Map<Long, String> finalWorkerNameMap = workerNameMap;
        Map<Long, String> finalWorkerPhoneMap = workerPhoneMap;
        Map<Long, String> finalWorkerGenderMap = workerGenderMap;
        Map<Long, Integer> finalWorkerAgeMap = workerAgeMap;
        List<ScheduleShiftVO> voList = shifts.stream()
                .map(s -> toShiftResponse(s, recordMap.get(s.getId()), finalJobMap, finalWorkerNameMap, finalWorkerPhoneMap, finalWorkerGenderMap, finalWorkerAgeMap, false))
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
        JobRate rate = jobRateMapper.findByJobId(shift.getJobId()).stream()
                .findFirst().orElse(null);
        if (rate != null) {
            shift.setSalaryType(rate.getType());
            shift.setSalaryAmount(rate.getAmount());
            shift.setSalaryCurrency(rate.getCurrency());
        }
        shiftMapper.update(shift);
        shift = shiftMapper.findById(id)
                .orElseThrow(() -> new RuntimeException("ScheduleShift not found: " + id));
        if (shift.getWorkerId() != null) {
            Job job = jobMapper.findById(shift.getJobId()).orElse(null);
            String jobTitle = job != null ? job.getTitle() : "岗位";
            String shiftInfo = jobTitle + " " + shift.getShiftDate() + " " + shift.getStartTime() + "-" + shift.getEndTime();
            workerNotificationMapper.insertWorkerNotification(shift.getWorkerId(), "SCHEDULE_UPDATED", "schedule",
                    "排班已变更", "您的排班已变更：" + shiftInfo, "SHIFT", shift.getId());
        }
        return toShiftResponse(shift);
    }

    @Override
    public void removeShift(Long id) {
        ScheduleShift shift = shiftMapper.findById(id)
                .orElseThrow(() -> new RuntimeException("ScheduleShift not found: " + id));
        if ("CANCELLED".equals(shift.getStatus())) {
            throw new RuntimeException("排班已取消");
        }
        if (shift.getShiftDate() == null || shift.getStartTime() == null
                || !LocalDateTime.of(shift.getShiftDate(), shift.getStartTime()).isAfter(LocalDateTime.now())) {
            throw new RuntimeException("只有未来的排班才能取消");
        }
        shiftMapper.cancelShift(id);
        if (shift.getWorkerId() != null) {
            Job job = jobMapper.findById(shift.getJobId()).orElse(null);
            String jobTitle = job != null ? job.getTitle() : "岗位";
            String shiftInfo = jobTitle + " " + shift.getShiftDate() + " " + shift.getStartTime() + "-" + shift.getEndTime();
            workerNotificationMapper.insertWorkerNotification(shift.getWorkerId(), "SCHEDULE_CANCELLED", "schedule",
                    "排班已取消", "您的排班已取消：" + shiftInfo, "SHIFT", shift.getId());
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
                Object bdRaw = m.get("birthday");
                java.time.LocalDate bd = bdRaw instanceof java.sql.Date sd ? sd.toLocalDate()
                        : bdRaw instanceof java.time.LocalDate ld ? ld : null;
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

    @Override
    public PageVO<ScheduleManagementVO> getManagedSchedules(Long companyId, Long jobId, String keyword, String status,
                                                            LocalDate startDate, LocalDate endDate, Integer page, Integer pageSize) {
        int currentPage = Math.max(page == null ? 1 : page, 1);
        int size = pageSize == null || pageSize <= 0 ? 20 : pageSize;
        int offset = (currentPage - 1) * size;
        String normalizedStatus = status == null || status.isBlank() ? null : status.trim().toUpperCase();
        List<ScheduleManagementVO> records = jobScheduleMapper.findManagementPage(companyId, jobId, keyword, normalizedStatus, startDate, endDate, offset, size);
        long total = jobScheduleMapper.countManagementPage(companyId, jobId, keyword, normalizedStatus, startDate, endDate);
        return new PageVO<>(records, total);
    }

    @Override
    public PageVO<ScheduleApplicantVO> getScheduleApplicants(Long scheduleId, String status, Integer page, Integer pageSize) {
        int currentPage = Math.max(page == null ? 1 : page, 1);
        int size = pageSize == null || pageSize <= 0 ? 20 : pageSize;
        int offset = (currentPage - 1) * size;
        String normalizedStatus = status == null || status.isBlank() ? null : status.trim().toUpperCase();
        return new PageVO<>(
                jobScheduleMapper.findApplicants(scheduleId, normalizedStatus, offset, size),
                jobScheduleMapper.countApplicants(scheduleId, normalizedStatus)
        );
    }

    @Override
    public ScheduleManagementVO updateManagedSchedule(ScheduleManageUpdateCmd request) {
        JobSchedule schedule = jobScheduleMapper.findById(request.getId())
                .orElseThrow(() -> new RuntimeException("JobSchedule not found: " + request.getId()));
        int acceptedCount = scheduleApplicationMapper.countByScheduleIdAndStatus(schedule.getId(), "ACCEPTED");
        Integer newCapacity = request.getSlotsAvailable() == null ? schedule.getSlotsAvailable() : request.getSlotsAvailable();
        if (newCapacity != null && newCapacity < acceptedCount) {
            throw new RuntimeException("招聘人数不能低于已通过人数");
        }
        boolean started = schedule.getScheduleDate() != null && schedule.getStartTime() != null
                && LocalDateTime.of(schedule.getScheduleDate(), schedule.getStartTime()).isBefore(LocalDateTime.now());
        if (started && "CANCELLED".equals(request.getStatus())) {
            throw new RuntimeException("只能取消未开始的班次");
        }
        if (!started) {
            if (request.getScheduleDate() != null) schedule.setScheduleDate(request.getScheduleDate());
            if (request.getStartTime() != null) schedule.setStartTime(request.getStartTime());
            if (request.getEndTime() != null) schedule.setEndTime(request.getEndTime());
            schedule.setSlotsAvailable(newCapacity);
        }
        if (request.getScheduleName() != null) schedule.setScheduleName(request.getScheduleName());
        if (request.getContactName() != null) schedule.setContactName(request.getContactName());
        if (request.getContactPhone() != null) schedule.setContactPhone(request.getContactPhone());
        if (request.getStatus() != null) schedule.setStatus(request.getStatus());
        jobScheduleMapper.update(schedule);
        return findManagedSchedule(schedule.getId());
    }

    @Override
    public ScheduleManagementVO copyManagedSchedule(ScheduleCopyCmd request) {
        JobSchedule source = jobScheduleMapper.findById(request.getSourceScheduleId())
                .orElseThrow(() -> new RuntimeException("JobSchedule not found: " + request.getSourceScheduleId()));
        JobSchedule target = new JobSchedule();
        target.setJobId(source.getJobId());
        target.setScheduleDate(request.getScheduleDate());
        target.setStartTime(request.getStartTime() == null ? source.getStartTime() : request.getStartTime());
        target.setEndTime(request.getEndTime() == null ? source.getEndTime() : request.getEndTime());
        target.setScheduleName(source.getScheduleName());
        target.setSlotsAvailable(source.getSlotsAvailable());
        target.setContactName(source.getContactName());
        target.setContactPhone(source.getContactPhone());
        target.setStatus("ACTIVE");
        jobScheduleMapper.insert(target);
        return findManagedSchedule(target.getId());
    }

    @Override
    public List<ScheduleManagementVO> batchCreateManagedSchedules(ScheduleBatchCreateCmd request) {
        Job job = jobMapper.findById(request.getJobId())
                .orElseThrow(() -> new RuntimeException("Job not found: " + request.getJobId()));
        List<ScheduleManagementVO> result = new ArrayList<>();
        LocalDate cursor = request.getStartDate();
        while (cursor != null && !cursor.isAfter(request.getEndDate())) {
            int weekday = cursor.getDayOfWeek().getValue();
            if (request.getWeekdays() == null || request.getWeekdays().isEmpty() || request.getWeekdays().contains(weekday)) {
                JobSchedule schedule = new JobSchedule();
                schedule.setJobId(job.getId());
                schedule.setScheduleDate(cursor);
                schedule.setStartTime(request.getStartTime());
                schedule.setEndTime(request.getEndTime());
                schedule.setScheduleName(job.getTitle());
                schedule.setSlotsAvailable(job.getHeadcount());
                schedule.setContactName(job.getContactName());
                schedule.setContactPhone(job.getContactPhone());
                schedule.setStatus("ACTIVE");
                jobScheduleMapper.insert(schedule);
                result.add(findManagedSchedule(schedule.getId()));
            }
            cursor = cursor.plusDays(1);
        }
        return result;
    }

    @Override
    public ScheduleExportVO exportScheduleApplicants(ScheduleExportCmd request) {
        JobSchedule schedule = jobScheduleMapper.findById(request.getScheduleId())
                .orElseThrow(() -> new RuntimeException("JobSchedule not found: " + request.getScheduleId()));
        String normalizedStatus = request.getStatus() == null || request.getStatus().isBlank() ? null : request.getStatus().trim().toUpperCase();
        List<ScheduleApplicantVO> applicants = jobScheduleMapper.findApplicants(schedule.getId(), normalizedStatus, 0, 10000);
        StringBuilder content = new StringBuilder("姓名,手机号,实名状态,报名时间,报名状态,排班状态,签到时间,签退时间,考勤状态,补卡状态,结算状态\n");
        for (ScheduleApplicantVO applicant : applicants) {
            appendCsvRow(content,
                    applicant.getWorkerName(),
                    applicant.getWorkerPhone(),
                    Boolean.TRUE.equals(applicant.getRealNamed()) ? "已实名" : "未实名",
                    formatValue(applicant.getAppliedAt()),
                    applicationStatusText(applicant.getApplicationStatus()),
                    shiftStatusText(applicant.getShiftStatus()),
                    formatValue(applicant.getCheckInTime()),
                    formatValue(applicant.getCheckOutTime()),
                    attendanceStatusText(applicant.getAttendanceStatus()),
                    correctionStatusText(applicant.getCorrectionStatus()),
                    settlementStatusText(applicant.getSettlementStatus()));
        }
        ScheduleExportVO result = new ScheduleExportVO();
        result.setFilename("schedule-" + schedule.getId() + "-applicants.csv");
        result.setContent('\ufeff' + content.toString());
        return result;
    }

    private void appendCsvRow(StringBuilder content, String... values) {
        for (int index = 0; index < values.length; index++) {
            if (index > 0) content.append(',');
            content.append(escapeCsv(values[index]));
        }
        content.append('\n');
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        String escaped = value.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\n") || escaped.contains("\r") || escaped.contains("\"")) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }

    private String formatValue(Object value) {
        return value == null ? "" : value.toString();
    }

    private String applicationStatusText(String status) {
        if ("PENDING".equals(status)) return "待审核";
        if ("ACCEPTED".equals(status)) return "已通过";
        if ("REJECTED".equals(status)) return "已拒绝";
        return formatValue(status);
    }

    private String shiftStatusText(String status) {
        if ("SCHEDULED".equals(status)) return "待上岗";
        if ("ON_DUTY".equals(status)) return "工作中";
        if ("COMPLETED".equals(status)) return "已完成";
        if ("ABSENT".equals(status)) return "缺勤";
        if ("LATE".equals(status)) return "迟到";
        if ("EARLY_LEAVE".equals(status) || "EARLY".equals(status)) return "早退";
        if ("LATE_EARLY_LEAVE".equals(status)) return "迟到并早退";
        if ("CANCELLED".equals(status)) return "已取消";
        return formatValue(status);
    }

    private String attendanceStatusText(String status) {
        if ("CHECKED_IN".equals(status)) return "已签到";
        if ("CHECKED_OUT".equals(status)) return "已签退";
        if ("NORMAL".equals(status) || "COMPLETED".equals(status)) return "正常";
        if ("ABSENT".equals(status)) return "缺勤";
        if ("LATE".equals(status)) return "迟到";
        if ("EARLY_LEAVE".equals(status) || "EARLY".equals(status)) return "早退";
        if ("LATE_EARLY_LEAVE".equals(status)) return "迟到并早退";
        return formatValue(status);
    }

    private String correctionStatusText(String status) {
        if ("PENDING".equals(status)) return "审批中";
        if ("APPROVED".equals(status)) return "已通过";
        if ("REJECTED".equals(status)) return "已拒绝";
        return formatValue(status);
    }

    private String settlementStatusText(String status) {
        if ("UNSETTLED".equals(status) || "UNPAID".equals(status)) return "未结算";
        if ("PAYING".equals(status)) return "结算中";
        if ("PENDING".equals(status)) return "待结算";
        if ("SETTLED".equals(status) || "PAID".equals(status)) return "已结算";
        return formatValue(status);
    }

    private ScheduleManagementVO findManagedSchedule(Long scheduleId) {
        JobSchedule schedule = jobScheduleMapper.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("JobSchedule not found: " + scheduleId));
        Job job = jobMapper.findById(schedule.getJobId())
                .orElseThrow(() -> new RuntimeException("Job not found: " + schedule.getJobId()));
        return jobScheduleMapper.findManagementPage(job.getCompanyId(), schedule.getJobId(), null, null,
                        schedule.getScheduleDate(), schedule.getScheduleDate(), 0, 1000)
                .stream()
                .filter(item -> scheduleId.equals(item.getId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("JobSchedule not found: " + scheduleId));
    }

    private ScheduleShiftVO toShiftResponse(ScheduleShift shift) {
        return toShiftResponse(shift, null, new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>(), true);
    }

    private ScheduleShiftVO toShiftResponse(ScheduleShift shift, AttendanceRecord record) {
        return toShiftResponse(shift, record, new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>(), true);
    }

    private ScheduleShiftVO toShiftResponse(ScheduleShift shift, AttendanceRecord record,
                                             Map<Long, Job> jobMap, Map<Long, String> workerNameMap,
                                             Map<Long, String> workerPhoneMap, Map<Long, String> workerGenderMap,
                                             Map<Long, Integer> workerAgeMap, boolean allowFallback) {
        ScheduleShiftVO response = new ScheduleShiftVO();
        response.setId(shift.getId());
        response.setJobId(shift.getJobId());
        Job job = jobMap.get(shift.getJobId());
        response.setJobTitle(job != null ? job.getTitle() : allowFallback ? jobMapper.findById(shift.getJobId()).map(Job::getTitle).orElse(null) : null);
        String workerName = workerNameMap.get(shift.getWorkerId());
        response.setWorkerName(workerName != null ? workerName : allowFallback ? workerSyncMapper.findWorkerNameById(shift.getWorkerId()) : null);
        response.setWorkerPhone(workerPhoneMap.get(shift.getWorkerId()));
        response.setWorkerGender(workerGenderMap.get(shift.getWorkerId()));
        Integer workerAge = workerAgeMap.get(shift.getWorkerId());
        if (workerAge != null) {
            response.setWorkerAge(workerAge);
        } else if (allowFallback) {
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
