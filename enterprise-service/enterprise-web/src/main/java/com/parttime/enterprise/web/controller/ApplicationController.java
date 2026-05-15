package com.parttime.enterprise.web.controller;

import com.parttime.enterprise.api.dto.JobApplicationResponse;
import com.parttime.enterprise.core.service.ApplicationService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/jobs/{jobId}/applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @GetMapping
    public List<JobApplicationResponse> getApplicationsByJob(@PathVariable Long jobId) {
        return applicationService.getApplicationsByJob(jobId);
    }

    @PutMapping("/{applicationId}/accept")
    public JobApplicationResponse acceptApplication(@PathVariable Long applicationId) {
        return applicationService.acceptApplication(applicationId);
    }

    @PutMapping("/{applicationId}/reject")
    public JobApplicationResponse rejectApplication(@PathVariable Long applicationId) {
        return applicationService.rejectApplication(applicationId);
    }
}
