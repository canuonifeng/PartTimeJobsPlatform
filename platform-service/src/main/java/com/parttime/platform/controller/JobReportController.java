package com.parttime.platform.controller;

import com.parttime.platform.pojo.cmd.ReviewJobReportCmd;
import com.parttime.platform.pojo.vo.JobReportVO;
import com.parttime.platform.service.JobReportService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/job-reports")
public class JobReportController {

    private final JobReportService jobReportService;

    public JobReportController(JobReportService jobReportService) {
        this.jobReportService = jobReportService;
    }

    @GetMapping
    public List<JobReportVO> list(@RequestParam(required = false) String status) {
        return jobReportService.getJobReports(status);
    }

    @GetMapping("/{id}")
    public JobReportVO get(@PathVariable Long id) {
        return jobReportService.getJobReport(id);
    }

    @PutMapping("/{id}/dismiss")
    public JobReportVO dismiss(@PathVariable Long id,
                                      @RequestBody ReviewJobReportCmd cmd,
                                      Authentication authentication) {
        return jobReportService.dismissReport(id, authentication.getName(), cmd);
    }

    @PutMapping("/{id}/ban")
    public JobReportVO ban(@PathVariable Long id,
                                  @RequestBody ReviewJobReportCmd cmd,
                                  Authentication authentication) {
        return jobReportService.banJobReport(id, authentication.getName(), cmd);
    }
}
