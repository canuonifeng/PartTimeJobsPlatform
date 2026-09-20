package com.parttime.platform.controller;

import com.parttime.platform.pojo.cmd.IdCmd;
import com.parttime.platform.pojo.cmd.JobIdCmd;
import com.parttime.platform.pojo.cmd.JobQueryCmd;
import com.parttime.platform.pojo.vo.ApiResponse;
import com.parttime.platform.pojo.vo.JobScheduleVO;
import com.parttime.platform.service.JobScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/schedules")
public class JobScheduleController {

    @Resource
    private JobScheduleService jobScheduleService;

    @Operation(summary = "排班列表")
    @PostMapping("/list")
    public ApiResponse<List<JobScheduleVO>> list(@RequestBody(required = false) JobQueryCmd body) {
        JobQueryCmd cmd = body != null ? body : new JobQueryCmd();
        return ApiResponse.success(jobScheduleService.list(cmd));
    }

    @Operation(summary = "获取职位的排班列表")
    @PostMapping("/list-by-job")
    public ApiResponse<List<JobScheduleVO>> listByJobId(@RequestBody JobIdCmd body) {
        return ApiResponse.success(jobScheduleService.listByJobId(body.getJobId()));
    }

    @Operation(summary = "获取排班详情")
    @PostMapping("/detail")
    public ApiResponse<JobScheduleVO> detail(@RequestBody IdCmd body) {
        return ApiResponse.success(jobScheduleService.detail(body.getId()));
    }

    @Operation(summary = "取消排班")
    @PostMapping("/cancel")
    public ApiResponse<Void> cancel(@RequestBody IdCmd body) {
        jobScheduleService.cancel(body.getId());
        return ApiResponse.success();
    }
}
