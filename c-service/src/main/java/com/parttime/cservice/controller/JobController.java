package com.parttime.cservice.controller;

import com.parttime.cservice.pojo.cmd.ApplyJobCmd;
import com.parttime.cservice.pojo.vo.ApplicationVO;
import com.parttime.cservice.pojo.vo.JobDetailVO;
import com.parttime.cservice.pojo.vo.JobSummaryVO;
import com.parttime.cservice.service.JobService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @GetMapping
    public ResponseEntity<List<JobSummaryVO>> searchJobs(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) BigDecimal minRate,
            @RequestParam(required = false) BigDecimal maxRate) {
        List<JobSummaryVO> results = jobService.searchJobs(keyword, categoryId, location, minRate, maxRate);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobDetailVO> getJobDetail(@PathVariable Long id) {
        JobDetailVO detail = jobService.getJobDetail(id);
        return ResponseEntity.ok(detail);
    }

    @PostMapping("/{id}/apply")
    public ResponseEntity<?> applyForJob(@PathVariable Long id, @RequestBody ApplyJobCmd request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Long workerId = Long.valueOf(auth.getName());
        boolean success = jobService.applyForJob(workerId, id, request.scheduleIds());
        return ResponseEntity.ok(Map.of("success", success));
    }

    @GetMapping("/{id}/application")
    public ResponseEntity<?> getApplicationStatus(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Long workerId = Long.valueOf(auth.getName());
        List<ApplicationVO> statuses = jobService.getApplicationStatus(workerId, id);
        return ResponseEntity.ok(statuses);
    }
}
