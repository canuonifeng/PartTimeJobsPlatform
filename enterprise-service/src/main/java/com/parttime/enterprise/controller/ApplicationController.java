package com.parttime.enterprise.controller;

import com.parttime.enterprise.config.SecurityUtil;
import com.parttime.enterprise.pojo.vo.JobApplicationVO;
import com.parttime.enterprise.pojo.vo.PageVO;
import com.parttime.enterprise.service.ApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    @Resource
    private ApplicationService applicationService;

    @Operation(summary = "获取岗位申请列表", description = "根据企业ID获取所有申请记录")
    @GetMapping
    public PageVO<JobApplicationVO> getApplicationsByJob(
            @Parameter(description = "岗位ID") @RequestParam(required = false) Long jobId,
            @Parameter(description = "岗位标题") @RequestParam(required = false) String jobTitle,
            @Parameter(description = "状态") @RequestParam(required = false) String status,
            @Parameter(description = "页码") @RequestParam(required = false, defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(required = false, defaultValue = "20") Integer pageSize) {
        Long companyId = SecurityUtil.getCurrentCompanyId();
        return applicationService.getApplicationsByJob(companyId, jobId, jobTitle, status, page, pageSize);
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
