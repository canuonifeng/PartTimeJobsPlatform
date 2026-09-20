package com.parttime.platform.controller;

import com.parttime.platform.pojo.cmd.BatchIdCmd;
import com.parttime.platform.pojo.cmd.IdCmd;
import com.parttime.platform.pojo.cmd.JobIdCmd;
import com.parttime.platform.pojo.cmd.JobQueryCmd;
import com.parttime.platform.pojo.vo.ApiResponse;
import com.parttime.platform.pojo.vo.ApplicationVO;
import com.parttime.platform.service.ApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/applications")
public class ApplicationController {

    @Resource
    private ApplicationService applicationService;

    @Operation(summary = "获取报名列表")
    @PostMapping("/list")
    public ApiResponse<List<ApplicationVO>> list(@RequestBody(required = false) JobQueryCmd body) {
        JobQueryCmd cmd = body != null ? body : new JobQueryCmd();
        return ApiResponse.success(applicationService.list(cmd));
    }

    @Operation(summary = "按职位获取报名列表")
    @PostMapping("/list-by-job")
    public ApiResponse<List<ApplicationVO>> listByJobId(@RequestBody JobIdCmd body) {
        return ApiResponse.success(applicationService.listByJobId(body.getJobId()));
    }

    @Operation(summary = "获取报名详情")
    @PostMapping("/detail")
    public ApiResponse<ApplicationVO> detail(@RequestBody IdCmd body) {
        return ApiResponse.success(applicationService.detail(body.getId()));
    }

    @Operation(summary = "审核通过")
    @PostMapping("/accept")
    public ApiResponse<Void> accept(@RequestBody IdCmd body) {
        applicationService.accept(body.getId());
        return ApiResponse.success();
    }

    @Operation(summary = "审核拒绝")
    @PostMapping("/reject")
    public ApiResponse<Void> reject(@RequestBody IdCmd body) {
        applicationService.reject(body.getId());
        return ApiResponse.success();
    }

    @Operation(summary = "批量通过")
    @PostMapping("/batch-accept")
    public ApiResponse<Void> batchAccept(@RequestBody BatchIdCmd body) {
        applicationService.batchAccept(body.getIds());
        return ApiResponse.success();
    }

    @Operation(summary = "批量拒绝")
    @PostMapping("/batch-reject")
    public ApiResponse<Void> batchReject(@RequestBody BatchIdCmd body) {
        applicationService.batchReject(body.getIds());
        return ApiResponse.success();
    }
}
