package com.parttime.enterprise.controller;

import com.parttime.enterprise.config.SecurityUtil;
import com.parttime.enterprise.pojo.cmd.ApplicationActionCmd;
import com.parttime.enterprise.pojo.vo.ApiResponse;
import com.parttime.enterprise.pojo.vo.ScheduleApplicationVO;import com.parttime.enterprise.pojo.vo.PageVO;
import com.parttime.enterprise.service.ApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/enterprise/applications")
public class ApplicationController {

    @Resource
    private ApplicationService applicationService;

    @Operation(summary = "获取排班报名列表", description = "根据企业ID获取所有排班报名记录")
    @GetMapping
    public ApiResponse<PageVO<ScheduleApplicationVO>> getApplicationsByJob(
            @Parameter(description = "岗位ID") @RequestParam(required = false) Long jobId,
            @Parameter(description = "岗位标题") @RequestParam(required = false) String jobTitle,
            @Parameter(description = "班次ID") @RequestParam(required = false) Long scheduleId,
            @Parameter(description = "状态") @RequestParam(required = false) String status,
            @Parameter(description = "页码") @RequestParam(required = false, defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(required = false, defaultValue = "20") Integer pageSize) {
        Long companyId = SecurityUtil.getCurrentCompanyId();
        return ApiResponse.success(applicationService.getApplicationsByJob(companyId, jobId, jobTitle, scheduleId, status, page, pageSize));
    }

    @Operation(summary = "通过申请", description = "通过工人的岗位申请")
    @PostMapping("/accept")
    public ApiResponse<ScheduleApplicationVO> acceptApplication(@RequestBody ApplicationActionCmd cmd) {
        return ApiResponse.success(applicationService.acceptApplication(cmd.getApplicationId()));
    }

    @Operation(summary = "拒绝申请", description = "拒绝工人的岗位申请")
    @PostMapping("/reject")
    public ApiResponse<ScheduleApplicationVO> rejectApplication(@RequestBody ApplicationActionCmd cmd) {
        return ApiResponse.success(applicationService.rejectApplication(cmd.getApplicationId()));
    }
}
