package com.parttime.enterprise.web.controller;

import com.parttime.enterprise.api.dto.JobRateRequest;
import com.parttime.enterprise.api.dto.JobRateResponse;
import com.parttime.enterprise.api.dto.JobResponse;
import com.parttime.enterprise.api.dto.JobScheduleRequest;
import com.parttime.enterprise.api.dto.JobScheduleResponse;
import com.parttime.enterprise.core.service.JobService;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    // === 3.3 Status Management ===

    @PutMapping("/{id}/publish")
    public JobResponse publishJob(@PathVariable Long id) {
        return jobService.publishJob(id);
    }

    @PutMapping("/{id}/close")
    public JobResponse closeJob(@PathVariable Long id) {
        return jobService.closeJob(id);
    }

    @PutMapping("/{id}/reopen")
    public JobResponse reopenJob(@PathVariable Long id) {
        return jobService.reopenJob(id);
    }

    // === 3.4 Mixed Salary Rate Configuration ===

    @GetMapping("/{jobId}/rates")
    public List<JobRateResponse> getJobRates(@PathVariable Long jobId) {
        return jobService.getJobRates(jobId);
    }

    @PostMapping("/{jobId}/rates")
    @ResponseStatus(HttpStatus.CREATED)
    public JobRateResponse addJobRate(@PathVariable Long jobId, @RequestBody JobRateRequest request) {
        return jobService.addJobRate(jobId, request);
    }

    @PutMapping("/{jobId}/rates/{rateId}")
    public JobRateResponse updateJobRate(@PathVariable Long jobId, @PathVariable Long rateId,
                                         @RequestBody JobRateRequest request) {
        return jobService.updateJobRate(rateId, request);
    }

    @DeleteMapping("/{jobId}/rates/{rateId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeJobRate(@PathVariable Long jobId, @PathVariable Long rateId) {
        jobService.removeJobRate(rateId);
    }

    // === 3.5 Optional Schedule Configuration ===

    @GetMapping("/{jobId}/schedules")
    public List<JobScheduleResponse> getJobSchedules(@PathVariable Long jobId) {
        return jobService.getJobSchedules(jobId);
    }

    @PostMapping("/{jobId}/schedules")
    @ResponseStatus(HttpStatus.CREATED)
    public JobScheduleResponse addJobSchedule(@PathVariable Long jobId, @RequestBody JobScheduleRequest request) {
        return jobService.addJobSchedule(jobId, request);
    }

    @PutMapping("/{jobId}/schedules/{scheduleId}")
    public JobScheduleResponse updateJobSchedule(@PathVariable Long jobId, @PathVariable Long scheduleId,
                                                  @RequestBody JobScheduleRequest request) {
        return jobService.updateJobSchedule(scheduleId, request);
    }

    @DeleteMapping("/{jobId}/schedules/{scheduleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeJobSchedule(@PathVariable Long jobId, @PathVariable Long scheduleId) {
        jobService.removeJobSchedule(scheduleId);
    }
}