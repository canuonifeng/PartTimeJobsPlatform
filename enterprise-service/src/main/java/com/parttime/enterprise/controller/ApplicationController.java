package com.parttime.enterprise.controller;

import com.parttime.enterprise.pojo.vo.JobApplicationVO;
import com.parttime.enterprise.service.ApplicationService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    @Resource
    private ApplicationService applicationService;

    @GetMapping
    public List<JobApplicationVO> getApplicationsByJob(@RequestParam Long jobId) {
        return applicationService.getApplicationsByJob(jobId);
    }

    @PutMapping("/accept")
    public JobApplicationVO acceptApplication(@RequestParam Long applicationId) {
        return applicationService.acceptApplication(applicationId);
    }

    @PutMapping("/reject")
    public JobApplicationVO rejectApplication(@RequestParam Long applicationId) {
        return applicationService.rejectApplication(applicationId);
    }
}
