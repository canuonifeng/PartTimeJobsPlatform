package com.parttime.platform.web.controller;

import com.parttime.platform.api.dto.JobReportResponse;
import com.parttime.platform.api.dto.JobReportReviewRequest;
import com.parttime.platform.core.service.JobReportService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/job-reports")
public class JobReportController {

    private final JobReportService jobReportService;

    public JobReportController(JobReportService jobReportService) {
        this.jobReportService = jobReportService;
    }

    @GetMapping
    public List<JobReportResponse> list(@RequestParam(required = false) String status) {
        return jobReportService.getJobReports(status);
    }

    @GetMapping("/{id}")
    public JobReportResponse get(@PathVariable Long id) {
        return jobReportService.getJobReport(id);
    }

    @PutMapping("/{id}/dismiss")
    public JobReportResponse dismiss(@PathVariable Long id,
                                      @RequestBody JobReportReviewRequest request,
                                      Authentication authentication) {
        return jobReportService.dismissReport(id, authentication.getName(), request);
    }

    @PutMapping("/{id}/ban")
    public JobReportResponse ban(@PathVariable Long id,
                                  @RequestBody JobReportReviewRequest request,
                                  Authentication authentication) {
        return jobReportService.banJobReport(id, authentication.getName(), request);
    }
}
