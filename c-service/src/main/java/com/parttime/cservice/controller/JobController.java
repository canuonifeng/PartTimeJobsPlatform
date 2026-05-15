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

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    @Resource
    private JobService jobService;

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

    @GetMapping("/detail")
    public ResponseEntity<JobDetailVO> getJobDetail(@RequestParam Long id) {
        JobDetailVO detail = jobService.getJobDetail(id);
        return ResponseEntity.ok(detail);
    }

    @PostMapping("/apply")
    public ResponseEntity<?> applyForJob(@RequestParam Long id, @RequestBody ApplyJobCmd request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Long workerId = Long.valueOf(auth.getName());
        boolean success = jobService.applyForJob(workerId, id, request.scheduleIds());
        return ResponseEntity.ok(Map.of("success", success));
    }

    @GetMapping("/application")
    public ResponseEntity<?> getApplicationStatus(@RequestParam Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Long workerId = Long.valueOf(auth.getName());
        List<ApplicationVO> statuses = jobService.getApplicationStatus(workerId, id);
        return ResponseEntity.ok(statuses);
    }
}
