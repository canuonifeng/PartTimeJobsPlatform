package com.parttime.enterprise.controller;

import com.parttime.enterprise.pojo.cmd.JobRateCmd;
import com.parttime.enterprise.pojo.cmd.JobScheduleCmd;
import com.parttime.enterprise.pojo.vo.JobRateVO;
import com.parttime.enterprise.pojo.vo.JobScheduleVO;
import com.parttime.enterprise.pojo.vo.JobVO;
import com.parttime.enterprise.service.JobService;

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

    @PutMapping("/{id}/publish")
    public JobVO publishJob(@PathVariable Long id) {
        return jobService.publishJob(id);
    }

    @PutMapping("/{id}/close")
    public JobVO closeJob(@PathVariable Long id) {
        return jobService.closeJob(id);
    }

    @PutMapping("/{id}/reopen")
    public JobVO reopenJob(@PathVariable Long id) {
        return jobService.reopenJob(id);
    }

    @GetMapping("/{jobId}/rates")
    public List<JobRateVO> getJobRates(@PathVariable Long jobId) {
        return jobService.getJobRates(jobId);
    }

    @PostMapping("/{jobId}/rates")
    @ResponseStatus(HttpStatus.CREATED)
    public JobRateVO addJobRate(@PathVariable Long jobId, @RequestBody JobRateCmd request) {
        return jobService.addJobRate(jobId, request);
    }

    @PutMapping("/{jobId}/rates/{rateId}")
    public JobRateVO updateJobRate(@PathVariable Long jobId, @PathVariable Long rateId,
                                   @RequestBody JobRateCmd request) {
        return jobService.updateJobRate(rateId, request);
    }

    @DeleteMapping("/{jobId}/rates/{rateId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeJobRate(@PathVariable Long jobId, @PathVariable Long rateId) {
        jobService.removeJobRate(rateId);
    }

    @GetMapping("/{jobId}/schedules")
    public List<JobScheduleVO> getJobSchedules(@PathVariable Long jobId) {
        return jobService.getJobSchedules(jobId);
    }

    @PostMapping("/{jobId}/schedules")
    @ResponseStatus(HttpStatus.CREATED)
    public JobScheduleVO addJobSchedule(@PathVariable Long jobId, @RequestBody JobScheduleCmd request) {
        return jobService.addJobSchedule(jobId, request);
    }

    @PutMapping("/{jobId}/schedules/{scheduleId}")
    public JobScheduleVO updateJobSchedule(@PathVariable Long jobId, @PathVariable Long scheduleId,
                                           @RequestBody JobScheduleCmd request) {
        return jobService.updateJobSchedule(scheduleId, request);
    }

    @DeleteMapping("/{jobId}/schedules/{scheduleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeJobSchedule(@PathVariable Long jobId, @PathVariable Long scheduleId) {
        jobService.removeJobSchedule(scheduleId);
    }
}
