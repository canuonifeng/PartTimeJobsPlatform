package com.parttime.enterprise.service.impl;

import com.parttime.enterprise.enums.ApplicationStatus;
import com.parttime.enterprise.exception.BusinessException;
import com.parttime.enterprise.mapper.CompanyWorkerMapper;
import com.parttime.enterprise.mapper.JobApplicationMapper;
import com.parttime.enterprise.mapper.JobMapper;
import com.parttime.enterprise.mapper.WorkerSyncMapper;
import com.parttime.enterprise.pojo.entity.Job;
import com.parttime.enterprise.pojo.entity.JobApplication;
import com.parttime.enterprise.pojo.vo.JobApplicationVO;
import com.parttime.enterprise.service.ApplicationService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ApplicationServiceImpl implements ApplicationService {

    @Resource
    private JobApplicationMapper applicationMapper;
    @Resource
    private JobMapper jobMapper;
    @Resource
    private WorkerSyncMapper workerSyncMapper;
    @Resource
    private CompanyWorkerMapper companyWorkerMapper;

    @Override
    public List<JobApplicationVO> getApplicationsByJob(Long jobId, String jobTitle, String status, Integer page, Integer pageSize) {
        List<JobApplication> apps;
        if (jobId != null) {
            apps = applicationMapper.findByJobId(jobId);
        } else {
            apps = applicationMapper.findAll();
        }
        List<JobApplication> filtered = apps.stream()
                .filter(app -> status == null || status.isEmpty() || status.equals(app.getStatus()))
                .filter(app -> {
                    if (jobTitle == null || jobTitle.isEmpty()) return true;
                    Job job = jobMapper.findById(app.getJobId()).orElse(null);
                    return job != null && job.getTitle() != null && job.getTitle().contains(jobTitle);
                })
                .collect(Collectors.toList());
        if (page != null && pageSize != null) {
            int fromIndex = Math.max(page - 1, 0) * pageSize;
            if (fromIndex >= filtered.size()) {
                return List.of();
            }
            int toIndex = Math.min(fromIndex + pageSize, filtered.size());
            filtered = filtered.subList(fromIndex, toIndex);
        }
        return filtered.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<JobApplicationVO> getApplicationsByWorker(Long workerId) {
        return applicationMapper.findByWorkerId(workerId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public JobApplicationVO acceptApplication(Long applicationId) {
        JobApplication app = applicationMapper.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found: " + applicationId));

        Job job = jobMapper.findById(app.getJobId())
                .orElseThrow(() -> new RuntimeException("Job not found: " + app.getJobId()));

        int acceptedCount = applicationMapper.countByJobIdAndStatus(app.getJobId(), "ACCEPTED");
        if (acceptedCount >= job.getHeadcount()) {
            throw new BusinessException("岗位已录满");
        }

        applicationMapper.updateStatus(applicationId, "ACCEPTED");
        app.setStatus("ACCEPTED");
        companyWorkerMapper.upsert(job.getCompanyId(), app.getWorkerId());
        return toResponse(app);
    }

    @Override
    public JobApplicationVO rejectApplication(Long applicationId) {
        JobApplication app =         applicationMapper.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found: " + applicationId));

        applicationMapper.updateStatus(applicationId, "REJECTED");
        app.setStatus("REJECTED");
        return toResponse(app);
    }

    private JobApplicationVO toResponse(JobApplication app) {
        JobApplicationVO response = new JobApplicationVO();
        response.setId(app.getId());
        response.setJobId(app.getJobId());
        response.setWorkerId(app.getWorkerId());
        response.setStatus(ApplicationStatus.valueOf(app.getStatus()));
        response.setAppliedAt(app.getAppliedAt());
        response.setUpdatedAt(app.getUpdatedAt());

        Job job = jobMapper.findById(app.getJobId()).orElse(null);
        if (job != null) {
            response.setJobTitle(job.getTitle());
        }
        response.setWorkerName(workerSyncMapper.findWorkerNameById(app.getWorkerId()));
        response.setWorkerPhone(workerSyncMapper.findWorkerPhoneById(app.getWorkerId()));

        return response;
    }
}
