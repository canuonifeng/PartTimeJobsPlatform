package com.parttime.platform.controller;

import com.parttime.platform.pojo.cmd.ReviewJobReportCmd;
import com.parttime.platform.pojo.vo.JobReportVO;
import com.parttime.platform.service.JobReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/platform/job-reports")
public class JobReportController {

    @Resource
    private JobReportService jobReportService;

    @Operation(summary = "获取举报列表", description = "根据状态获取岗位举报列表")
    @GetMapping
    public List<JobReportVO> list(@Parameter(description = "处理状态: PENDING-待处理, DISMISSED-已驳回, BANNED-已封禁") @RequestParam(required = false) String status) {
        return jobReportService.getJobReports(status);
    }

    @Operation(summary = "获取举报详情", description = "根据ID获取岗位举报详情")
    @GetMapping(params = "id")
    public JobReportVO get(@Parameter(description = "举报ID") @RequestParam Long id) {
        return jobReportService.getJobReport(id);
    }

    @Operation(summary = "驳回举报", description = "驳回岗位举报，标记为无效")
    @PutMapping("/dismiss")
    public JobReportVO dismiss(@Parameter(description = "举报ID") @RequestParam Long id,
                                      @RequestBody ReviewJobReportCmd cmd,
                                      Authentication authentication) {
        return jobReportService.dismissReport(id, authentication.getName(), cmd);
    }

    @Operation(summary = "封禁岗位", description = "因举报封禁岗位")
    @PutMapping("/ban")
    public JobReportVO ban(@Parameter(description = "举报ID") @RequestParam Long id,
                                  @RequestBody ReviewJobReportCmd cmd,
                                  Authentication authentication) {
        return jobReportService.banJobReport(id, authentication.getName(), cmd);
    }
}
