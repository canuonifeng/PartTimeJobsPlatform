package com.parttime.enterprise.service.impl;

import com.parttime.enterprise.enums.ApplicationStatus;
import com.parttime.enterprise.exception.BusinessException;
import com.parttime.enterprise.mapper.ScheduleApplicationMapper;
import com.parttime.enterprise.mapper.CompanyWorkerMapper;
import com.parttime.enterprise.mapper.ScheduleApplicationMapper;
import com.parttime.enterprise.mapper.JobMapper;
import com.parttime.enterprise.mapper.JobRateMapper;
import com.parttime.enterprise.mapper.JobScheduleMapper;
import com.parttime.enterprise.mapper.ScheduleShiftMapper;
import com.parttime.enterprise.mapper.WorkerNotificationMapper;
import com.parttime.enterprise.mapper.WorkerSyncMapper;
import com.parttime.enterprise.pojo.entity.Job;
import com.parttime.enterprise.pojo.entity.ScheduleApplication;
import com.parttime.enterprise.pojo.entity.JobRate;
import com.parttime.enterprise.pojo.entity.JobSchedule;
import com.parttime.enterprise.pojo.entity.ScheduleShift;
import com.parttime.enterprise.pojo.vo.ScheduleApplicationVO;
import com.parttime.enterprise.pojo.vo.PageVO;
import com.parttime.enterprise.service.ApplicationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DuplicateKeyException;

import javax.annotation.Resource;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ApplicationServiceImpl implements ApplicationService {

    @Resource
    private ScheduleApplicationMapper applicationMapper;
    @Resource
    private ScheduleApplicationMapper scheduleApplicationMapper;
    @Resource
    private JobMapper jobMapper;
    @Resource
    private JobScheduleMapper jobScheduleMapper;
    @Resource
    private JobRateMapper jobRateMapper;
    @Resource
    private ScheduleShiftMapper shiftMapper;
    @Resource
    private WorkerSyncMapper workerSyncMapper;
    @Resource
    private CompanyWorkerMapper companyWorkerMapper;
    @Resource
    private WorkerNotificationMapper workerNotificationMapper;

    @Override
    public PageVO<ScheduleApplicationVO> getApplicationsByJob(Long companyId, Long jobId, String jobTitle, String status, Integer page, Integer pageSize) {
        List<ScheduleApplicationVO> apps;
        if (jobId != null && status != null && !status.isEmpty()) {
            apps = scheduleApplicationMapper.findVOByJobIdAndStatus(jobId, status);
        } else if (jobId != null) {
            apps = scheduleApplicationMapper.findVOByJobId(jobId);
        } else if (status != null && !status.isEmpty()) {
            apps = scheduleApplicationMapper.findVOByCompanyIdAndStatus(companyId, status);
        } else {
            apps = scheduleApplicationMapper.findVOByCompanyId(companyId);
        }
        if (jobTitle != null && !jobTitle.isEmpty()) {
            apps = apps.stream()
                    .filter(a -> a.getJobTitle() != null && a.getJobTitle().contains(jobTitle))
                    .collect(Collectors.toList());
        }
        int total = apps.size();
        if (page != null && pageSize != null) {
            int fromIndex = Math.max(page - 1, 0) * pageSize;
            if (fromIndex >= apps.size()) {
                return new PageVO<>(List.of(), total);
            }
            int toIndex = Math.min(fromIndex + pageSize, apps.size());
            apps = apps.subList(fromIndex, toIndex);
        }
        return new PageVO<>(apps, total);
    }

    @Override
    public List<ScheduleApplicationVO> getApplicationsByWorker(Long workerId) {
        return applicationMapper.findByWorkerId(workerId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ScheduleApplicationVO acceptApplication(Long applicationId) {
        ScheduleApplication app = applicationMapper.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found: " + applicationId));

        JobSchedule schedule = jobScheduleMapper.findById(app.getScheduleId())
                .orElseThrow(() -> new RuntimeException("Schedule not found: " + app.getScheduleId()));
        Job job = jobMapper.findById(schedule.getJobId())
                .orElseThrow(() -> new RuntimeException("Job not found: " + schedule.getJobId()));

        if (!"ACCEPTED".equals(app.getStatus())) {
            int acceptedCount = applicationMapper.countByJobIdAndStatus(schedule.getJobId(), "ACCEPTED");
            if (acceptedCount >= job.getHeadcount()) {
                throw new BusinessException("岗位已录满");
            }

            applicationMapper.updateStatus(applicationId, "ACCEPTED");
            app.setStatus("ACCEPTED");
        }

        boolean shiftCreated = ensureShifts(app, job);
        companyWorkerMapper.upsert(job.getCompanyId(), app.getWorkerId());
        workerNotificationMapper.insertWorkerNotification(app.getWorkerId(), "APPLICATION_ACCEPTED", "application",
                "报名已通过", "您报名的" + job.getTitle() + "已通过审核", "APPLICATION", app.getId());
        if (shiftCreated) {
            workerNotificationMapper.insertWorkerNotification(app.getWorkerId(), "SCHEDULE_ASSIGNED", "schedule",
                    "排班已生成", "您报名的" + job.getTitle() + "已生成排班，请及时查看", "SHIFT", null);
        }
        return toResponse(app);
    }

    private boolean ensureShifts(ScheduleApplication app, Job job) {
        JobSchedule schedule = jobScheduleMapper.findById(app.getScheduleId())
                .orElseThrow(() -> new RuntimeException("Schedule not found: " + app.getScheduleId()));
        JobRate rate = jobRateMapper.findByJobId(job.getId()).stream()
                .findFirst()
                .orElse(null);

        boolean alreadyExists = shiftMapper.findByApplicationId(app.getId()).stream()
                .anyMatch(s -> s.getShiftDate().equals(schedule.getScheduleDate())
                        && s.getStartTime().equals(schedule.getStartTime())
                        && s.getEndTime().equals(schedule.getEndTime()));

        if (!alreadyExists) {
            ScheduleShift shift = new ScheduleShift();
            shift.setApplicationId(app.getId());
            shift.setJobId(job.getId());
            shift.setCompanyId(job.getCompanyId());
            shift.setWorkerId(app.getWorkerId());
            shift.setShiftDate(schedule.getScheduleDate());
            shift.setStartTime(schedule.getStartTime());
            shift.setEndTime(schedule.getEndTime());
            shift.setLocationLat(job.getLatitude());
            shift.setLocationLng(job.getLongitude());
            shift.setLocationName(job.getAddress());
            if (rate != null) {
                shift.setSalaryType(rate.getType());
                shift.setSalaryAmount(rate.getAmount());
                shift.setSalaryCurrency(rate.getCurrency());
            }
            try {
                shiftMapper.insert(shift);
                return true;
            } catch (DuplicateKeyException ignored) {
            }
        }
        return false;
    }

    @Override
    public ScheduleApplicationVO rejectApplication(Long applicationId) {
        ScheduleApplication app = applicationMapper.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found: " + applicationId));

        applicationMapper.updateStatus(applicationId, "REJECTED");
        app.setStatus("REJECTED");
        JobSchedule schedule = jobScheduleMapper.findById(app.getScheduleId()).orElse(null);
        Job job = schedule == null ? null : jobMapper.findById(schedule.getJobId()).orElse(null);
        String jobTitle = job == null ? "岗位" : job.getTitle();
        workerNotificationMapper.insertWorkerNotification(app.getWorkerId(), "APPLICATION_REJECTED", "application",
                "报名未通过", "您报名的" + jobTitle + "未通过审核", "APPLICATION", app.getId());
        return toResponse(app);
    }

    private ScheduleApplicationVO toResponse(ScheduleApplication app) {
        ScheduleApplicationVO response = new ScheduleApplicationVO();
        response.setId(app.getId());
        response.setScheduleId(app.getScheduleId());
        response.setWorkerId(app.getWorkerId());
        response.setStatus(ApplicationStatus.valueOf(app.getStatus()));
        response.setAppliedAt(app.getAppliedAt());
        response.setUpdatedAt(app.getUpdatedAt());

        JobSchedule schedule = jobScheduleMapper.findById(app.getScheduleId()).orElse(null);
        if (schedule != null) {
            response.setJobId(schedule.getJobId());
            Job job = jobMapper.findById(schedule.getJobId()).orElse(null);
            if (job != null) {
                response.setJobTitle(job.getTitle());
            }
        }
        response.setWorkerName(workerSyncMapper.findWorkerNameById(app.getWorkerId()));
        response.setWorkerPhone(workerSyncMapper.findWorkerPhoneById(app.getWorkerId()));
        java.time.LocalDate birthday = workerSyncMapper.findWorkerBirthdayById(app.getWorkerId());
        if (birthday != null) {
            response.setWorkerAge(java.time.LocalDate.now().getYear() - birthday.getYear());
        }

        return response;
    }
}
