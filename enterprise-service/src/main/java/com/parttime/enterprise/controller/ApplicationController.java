package com.parttime.enterprise.controller;

import com.parttime.enterprise.pojo.vo.JobApplicationVO;
import com.parttime.enterprise.service.ApplicationService;

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
    public List<JobApplicationVO> getApplicationsByJob(@PathVariable Long jobId) {
        return applicationService.getApplicationsByJob(jobId);
    }

    @PutMapping("/{applicationId}/accept")
    public JobApplicationVO acceptApplication(@PathVariable Long applicationId) {
        return applicationService.acceptApplication(applicationId);
    }

    @PutMapping("/{applicationId}/reject")
    public JobApplicationVO rejectApplication(@PathVariable Long applicationId) {
        return applicationService.rejectApplication(applicationId);
    }
}
