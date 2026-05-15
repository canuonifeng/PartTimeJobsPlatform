package com.parttime.enterprise.core.service;

import com.parttime.enterprise.api.dto.ApplicationStatus;
import com.parttime.enterprise.api.dto.JobApplicationResponse;
import com.parttime.enterprise.core.domain.Job;
import com.parttime.enterprise.core.domain.JobApplication;
import com.parttime.enterprise.core.exception.BusinessException;
import com.parttime.enterprise.core.repository.JobApplicationRepository;
import com.parttime.enterprise.core.repository.JobRepository;

import java.util.List;
import java.util.stream.Collectors;

public class ApplicationService {

    private final JobApplicationRepository applicationRepository;
    private final JobRepository jobRepository;

    public ApplicationService(JobApplicationRepository applicationRepository, JobRepository jobRepository) {
        this.applicationRepository = applicationRepository;
        this.jobRepository = jobRepository;
    }

    public List<JobApplicationResponse> getApplicationsByJob(Long jobId) {
        return applicationRepository.findByJobId(jobId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<JobApplicationResponse> getApplicationsByWorker(Long workerId) {
        return applicationRepository.findByWorkerId(workerId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public JobApplicationResponse acceptApplication(Long applicationId) {
        JobApplication app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found: " + applicationId));

        Job job = jobRepository.findById(app.getJobId())
                .orElseThrow(() -> new RuntimeException("Job not found: " + app.getJobId()));

        int acceptedCount = applicationRepository.countByJobIdAndStatus(app.getJobId(), "ACCEPTED");
        if (acceptedCount >= job.getHeadcount()) {
            throw new BusinessException("岗位已录满");
        }

        applicationRepository.updateStatus(applicationId, "ACCEPTED");
        app.setStatus("ACCEPTED");
        return toResponse(app);
    }

    public JobApplicationResponse rejectApplication(Long applicationId) {
        JobApplication app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found: " + applicationId));

        applicationRepository.updateStatus(applicationId, "REJECTED");
        app.setStatus("REJECTED");
        return toResponse(app);
    }

    private JobApplicationResponse toResponse(JobApplication app) {
        JobApplicationResponse response = new JobApplicationResponse();
        response.setId(app.getId());
        response.setJobId(app.getJobId());
        response.setWorkerId(app.getWorkerId());
        response.setStatus(ApplicationStatus.valueOf(app.getStatus()));
        response.setAppliedAt(app.getAppliedAt());
        response.setUpdatedAt(app.getUpdatedAt());
        return response;
    }
}
