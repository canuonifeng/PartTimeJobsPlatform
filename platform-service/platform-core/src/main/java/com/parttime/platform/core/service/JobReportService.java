package com.parttime.platform.core.service;

import com.parttime.platform.api.dto.JobReportResponse;
import com.parttime.platform.api.dto.JobReportReviewRequest;
import com.parttime.platform.core.domain.JobReport;
import com.parttime.platform.core.exception.BusinessException;
import com.parttime.platform.core.repository.JobReportRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class JobReportService {

    private final JobReportRepository jobReportRepository;

    public JobReportService(JobReportRepository jobReportRepository) {
        this.jobReportRepository = jobReportRepository;
    }

    public List<JobReportResponse> getJobReports(String status) {
        List<JobReport> list;
        if (status != null && !status.isBlank()) {
            list = jobReportRepository.findByStatus(status);
        } else {
            list = jobReportRepository.findAll();
        }
        return list.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public JobReportResponse getJobReport(Long id) {
        JobReport report = jobReportRepository.findById(id)
                .orElseThrow(() -> new BusinessException("JobReport not found: " + id));
        return toResponse(report);
    }

    public JobReportResponse dismissReport(Long id, String reviewerId, JobReportReviewRequest request) {
        JobReport report = jobReportRepository.findById(id)
                .orElseThrow(() -> new BusinessException("JobReport not found: " + id));
        if (!"PENDING".equals(report.getStatus())) {
            throw new BusinessException("Report is not in PENDING status");
        }
        report.setStatus("DISMISSED");
        report.setReviewerId(reviewerId);
        report.setReviewRemark(request.getRemark());
        report.setReviewedAt(LocalDateTime.now());
        jobReportRepository.update(report);
        return toResponse(report);
    }

    public JobReportResponse banJobReport(Long id, String reviewerId, JobReportReviewRequest request) {
        JobReport report = jobReportRepository.findById(id)
                .orElseThrow(() -> new BusinessException("JobReport not found: " + id));
        if (!"PENDING".equals(report.getStatus())) {
            throw new BusinessException("Report is not in PENDING status");
        }
        report.setStatus("BANNED");
        report.setReviewerId(reviewerId);
        report.setReviewRemark(request.getRemark());
        report.setReviewedAt(LocalDateTime.now());
        jobReportRepository.update(report);
        return toResponse(report);
    }

    private JobReportResponse toResponse(JobReport report) {
        JobReportResponse response = new JobReportResponse();
        response.setId(report.getId());
        response.setJobId(report.getJobId());
        response.setReporterId(report.getReporterId());
        response.setReason(report.getReason());
        response.setDescription(report.getDescription());
        response.setStatus(report.getStatus());
        response.setReviewerId(report.getReviewerId());
        response.setReviewRemark(report.getReviewRemark());
        response.setReviewedAt(report.getReviewedAt());
        response.setCreatedAt(report.getCreatedAt());
        response.setUpdatedAt(report.getUpdatedAt());
        return response;
    }
}
