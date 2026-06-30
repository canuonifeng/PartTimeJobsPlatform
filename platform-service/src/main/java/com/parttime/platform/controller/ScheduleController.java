package com.parttime.platform.controller;

import com.parttime.platform.pojo.cmd.IdCmd;
import com.parttime.platform.pojo.cmd.JobQueryCmd;
import com.parttime.platform.pojo.vo.ApiResponse;
import com.parttime.platform.pojo.vo.ScheduleVO;
import com.parttime.platform.service.ScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/schedules")
public class ScheduleController {

    @Resource
    private ScheduleService scheduleService;

    @Operation(summary = "排班列表")
    @PostMapping("/list")
    public ApiResponse<List<ScheduleVO>> list(@RequestBody(required = false) JobQueryCmd body) {
        JobQueryCmd cmd = body != null ? body : new JobQueryCmd();
        return ApiResponse.success(scheduleService.list(cmd));
    }

    @Operation(summary = "排班详情")
    @PostMapping("/detail")
    public ApiResponse<ScheduleVO> detail(@RequestBody IdCmd body) {
        return ApiResponse.success(scheduleService.detail(body.getId()));
    }

    @Operation(summary = "取消排班")
    @PostMapping("/cancel")
    public ApiResponse<Void> cancel(@RequestBody IdCmd body) {
        scheduleService.cancel(body.getId());
        return ApiResponse.success();
    }
}
