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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    @Resource
    private JobService jobService;

    @PutMapping("/publish")
    public JobVO publishJob(@RequestParam Long id) {
        return jobService.publishJob(id);
    }

    @PutMapping("/close")
    public JobVO closeJob(@RequestParam Long id) {
        return jobService.closeJob(id);
    }

    @PutMapping("/reopen")
    public JobVO reopenJob(@RequestParam Long id) {
        return jobService.reopenJob(id);
    }

    @GetMapping("/rates")
    public List<JobRateVO> getJobRates(@RequestParam Long jobId) {
        return jobService.getJobRates(jobId);
    }

    @PostMapping("/rates")
    @ResponseStatus(HttpStatus.CREATED)
    public JobRateVO addJobRate(@RequestParam Long jobId, @RequestBody JobRateCmd request) {
        return jobService.addJobRate(jobId, request);
    }

    @PutMapping("/rates")
    public JobRateVO updateJobRate(@RequestParam Long jobId, @RequestParam Long rateId,
                                   @RequestBody JobRateCmd request) {
        return jobService.updateJobRate(rateId, request);
    }

    @DeleteMapping("/rates")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeJobRate(@RequestParam Long jobId, @RequestParam Long rateId) {
        jobService.removeJobRate(rateId);
    }

    @GetMapping("/schedules")
    public List<JobScheduleVO> getJobSchedules(@RequestParam Long jobId) {
        return jobService.getJobSchedules(jobId);
    }

    @PostMapping("/schedules")
    @ResponseStatus(HttpStatus.CREATED)
    public JobScheduleVO addJobSchedule(@RequestParam Long jobId, @RequestBody JobScheduleCmd request) {
        return jobService.addJobSchedule(jobId, request);
    }

    @PutMapping("/schedules")
    public JobScheduleVO updateJobSchedule(@RequestParam Long jobId, @RequestParam Long scheduleId,
                                           @RequestBody JobScheduleCmd request) {
        return jobService.updateJobSchedule(scheduleId, request);
    }

    @DeleteMapping("/schedules")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeJobSchedule(@RequestParam Long jobId, @RequestParam Long scheduleId) {
        jobService.removeJobSchedule(scheduleId);
    }
}
