package com.parttime.enterprise.controller;

import com.parttime.enterprise.pojo.vo.JobApplicationVO;
import com.parttime.enterprise.service.ApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

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

    @Operation(summary = "获取岗位申请列表", description = "根据岗位ID获取所有申请记录")
    @GetMapping
    public List<JobApplicationVO> getApplicationsByJob(@Parameter(description = "岗位ID") @RequestParam Long jobId) {
        return applicationService.getApplicationsByJob(jobId);
    }

    @Operation(summary = "通过申请", description = "通过工人的岗位申请")
    @PutMapping("/accept")
    public JobApplicationVO acceptApplication(@Parameter(description = "申请ID") @RequestParam Long applicationId) {
        return applicationService.acceptApplication(applicationId);
    }

    @Operation(summary = "拒绝申请", description = "拒绝工人的岗位申请")
    @PutMapping("/reject")
    public JobApplicationVO rejectApplication(@Parameter(description = "申请ID") @RequestParam Long applicationId) {
        return applicationService.rejectApplication(applicationId);
    }
}
