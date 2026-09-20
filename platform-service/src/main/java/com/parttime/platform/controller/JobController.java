package com.parttime.platform.controller;

import com.parttime.platform.pojo.cmd.IdCmd;
import com.parttime.platform.pojo.cmd.JobQueryCmd;
import com.parttime.platform.pojo.cmd.JobRecommendedCmd;
import com.parttime.platform.pojo.cmd.JobTopCmd;
import com.parttime.platform.pojo.vo.ApiResponse;
import com.parttime.platform.pojo.vo.JobVO;
import com.parttime.platform.service.JobService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/jobs")
public class JobController {

    @Resource
    private JobService jobService;

    @Operation(summary = "获取职位列表")
    @PostMapping("/list")
    public ApiResponse<List<JobVO>> list(@RequestBody(required = false) JobQueryCmd body) {
        JobQueryCmd cmd = body != null ? body : new JobQueryCmd();
        return ApiResponse.success(jobService.list(cmd));
    }

    @Operation(summary = "获取职位详情")
    @PostMapping("/detail")
    public ApiResponse<JobVO> detail(@RequestBody IdCmd body) {
        return ApiResponse.success(jobService.detail(body.getId()));
    }

    @Operation(summary = "下架职位")
    @PostMapping("/close")
    public ApiResponse<Void> closeJob(@RequestBody IdCmd body) {
        jobService.closeJob(body.getId());
        return ApiResponse.success();
    }

    @Operation(summary = "重新上架职位")
    @PostMapping("/reopen")
    public ApiResponse<Void> reopenJob(@RequestBody IdCmd body) {
        jobService.reopenJob(body.getId());
        return ApiResponse.success();
    }

    @Operation(summary = "设置/取消置顶")
    @PostMapping("/set-top")
    public ApiResponse<Void> setTop(@RequestBody JobTopCmd body) {
        jobService.setTop(body.getId(), body.getIsTop());
        return ApiResponse.success();
    }

    @Operation(summary = "设置/取消推荐")
    @PostMapping("/set-recommended")
    public ApiResponse<Void> setRecommended(@RequestBody JobRecommendedCmd body) {
        jobService.setRecommended(body.getId(), body.getIsRecommended());
        return ApiResponse.success();
    }
}
