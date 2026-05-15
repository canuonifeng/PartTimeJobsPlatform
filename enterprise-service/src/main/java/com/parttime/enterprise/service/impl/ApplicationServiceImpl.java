package com.parttime.enterprise.service.impl;

import com.parttime.enterprise.enums.ApplicationStatus;
import com.parttime.enterprise.exception.BusinessException;
import com.parttime.enterprise.mapper.JobApplicationMapper;
import com.parttime.enterprise.mapper.JobMapper;
import com.parttime.enterprise.pojo.entity.Job;
import com.parttime.enterprise.pojo.entity.JobApplication;
import com.parttime.enterprise.pojo.vo.JobApplicationVO;
import com.parttime.enterprise.service.ApplicationService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ApplicationServiceImpl implements ApplicationService {

    private final JobApplicationMapper applicationMapper;
    private final JobMapper jobMapper;

    public ApplicationServiceImpl(JobApplicationMapper applicationMapper, JobMapper jobMapper) {
        this.applicationMapper = applicationMapper;
        this.jobMapper = jobMapper;
    }

    @Override
    public List<JobApplicationVO> getApplicationsByJob(Long jobId) {
        return applicationMapper.findByJobId(jobId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
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
        return response;
    }
}
