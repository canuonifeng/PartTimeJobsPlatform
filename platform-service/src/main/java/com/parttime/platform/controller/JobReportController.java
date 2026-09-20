package com.parttime.platform.controller;

import com.parttime.platform.pojo.cmd.IdCmd;
import com.parttime.platform.pojo.cmd.StatusQueryCmd;
import com.parttime.platform.pojo.vo.ApiResponse;
import com.parttime.platform.pojo.vo.JobReportVO;
import com.parttime.platform.service.JobReportService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/job-reports")
public class JobReportController {

    @Resource
    private JobReportService jobReportService;

    @Operation(summary = "获取举报列表")
    @PostMapping("/list")
    public ApiResponse<List<JobReportVO>> list(@RequestBody(required = false) StatusQueryCmd body) {
        String status = body != null ? body.getStatus() : null;
        return ApiResponse.success(jobReportService.list(status));
    }

    @Operation(summary = "获取举报详情")
    @PostMapping("/detail")
    public ApiResponse<JobReportVO> detail(@RequestBody IdCmd body) {
        return ApiResponse.success(jobReportService.detail(body.getId()));
    }

    @Operation(summary = "驳回举报")
    @PostMapping("/dismiss")
    public ApiResponse<Void> dismiss(@RequestBody IdCmd body) {
        jobReportService.dismiss(body.getId());
        return ApiResponse.success();
    }

    @Operation(summary = "封禁岗位")
    @PostMapping("/ban")
    public ApiResponse<Void> ban(@RequestBody IdCmd body) {
        jobReportService.ban(body.getId());
        return ApiResponse.success();
    }
}
