package com.parttime.cservice.web.controller;

import com.parttime.cservice.core.dto.ApplicationRequest;
import com.parttime.cservice.core.dto.ApplicationResponse;
import com.parttime.cservice.core.dto.JobDetail;
import com.parttime.cservice.core.dto.JobSummary;
import com.parttime.cservice.core.service.JobService;
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
    public ResponseEntity<List<JobSummary>> searchJobs(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) BigDecimal minRate,
            @RequestParam(required = false) BigDecimal maxRate) {
        List<JobSummary> results = jobService.searchJobs(keyword, categoryId, location, minRate, maxRate);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobDetail> getJobDetail(@PathVariable Long id) {
        JobDetail detail = jobService.getJobDetail(id);
        return ResponseEntity.ok(detail);
    }

    @PostMapping("/{id}/apply")
    public ResponseEntity<?> applyForJob(@PathVariable Long id, @RequestBody ApplicationRequest request) {
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
        List<ApplicationResponse> statuses = jobService.getApplicationStatus(workerId, id);
        return ResponseEntity.ok(statuses);
    }
}
