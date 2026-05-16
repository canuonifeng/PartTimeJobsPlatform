package com.parttime.enterprise.controller;

import com.parttime.enterprise.pojo.cmd.JobCreateCmd;
import com.parttime.enterprise.pojo.cmd.JobRateCmd;
import com.parttime.enterprise.pojo.cmd.JobScheduleCmd;
import com.parttime.enterprise.pojo.cmd.UpdateJobCmd;
import com.parttime.enterprise.pojo.vo.JobRateVO;
import com.parttime.enterprise.pojo.vo.JobScheduleVO;
import com.parttime.enterprise.pojo.vo.JobVO;
import com.parttime.enterprise.service.JobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

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

    @Operation(summary = "获取岗位列表")
    @GetMapping
    public List<JobVO> listJobs(@RequestParam(required = false) Long companyId,
                                @RequestParam(required = false) String status) {
        if (companyId == null) companyId = 1L;
        return jobService.getJobsByCompany(companyId, status);
    }

    @Operation(summary = "获取岗位详情")
    @GetMapping(params = "id")
    public JobVO getJob(@RequestParam Long id) {
        return jobService.getJobById(id);
    }

    @Operation(summary = "创建岗位")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public JobVO createJob(@RequestBody JobCreateCmd request) {
        return jobService.createJob(request);
    }

    @Operation(summary = "更新岗位")
    @PutMapping
    public JobVO updateJob(@RequestParam Long id, @RequestBody UpdateJobCmd request) {
        return jobService.updateJob(id, request);
    }

    @Operation(summary = "删除岗位")
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteJob(@RequestParam Long id) {
        jobService.deleteJob(id);
    }

    @Operation(summary = "发布岗位", description = "将草稿状态的岗位发布为已发布状态")
    @PutMapping("/publish")
    public JobVO publishJob(@Parameter(description = "岗位ID") @RequestParam Long id) {
        return jobService.publishJob(id);
    }

    @Operation(summary = "关闭岗位", description = "关闭已发布的岗位")
    @PutMapping("/close")
    public JobVO closeJob(@Parameter(description = "岗位ID") @RequestParam Long id) {
        return jobService.closeJob(id);
    }

    @Operation(summary = "重新开放岗位", description = "重新开放已关闭的岗位")
    @PutMapping("/reopen")
    public JobVO reopenJob(@Parameter(description = "岗位ID") @RequestParam Long id) {
        return jobService.reopenJob(id);
    }

    @Operation(summary = "获取岗位薪资规则", description = "根据岗位ID获取所有薪资规则")
    @GetMapping("/rates")
    public List<JobRateVO> getJobRates(@Parameter(description = "岗位ID") @RequestParam Long jobId) {
        return jobService.getJobRates(jobId);
    }

    @Operation(summary = "添加薪资规则", description = "为岗位添加一条薪资规则")
    @PostMapping("/rates")
    @ResponseStatus(HttpStatus.CREATED)
    public JobRateVO addJobRate(@Parameter(description = "岗位ID") @RequestParam Long jobId, @RequestBody JobRateCmd request) {
        return jobService.addJobRate(jobId, request);
    }

    @Operation(summary = "更新薪资规则", description = "更新岗位的薪资规则")
    @PutMapping("/rates")
    public JobRateVO updateJobRate(@Parameter(description = "岗位ID") @RequestParam Long jobId,
                                   @Parameter(description = "薪资规则ID") @RequestParam Long rateId,
                                   @RequestBody JobRateCmd request) {
        return jobService.updateJobRate(rateId, request);
    }

    @Operation(summary = "删除薪资规则", description = "删除岗位的薪资规则")
    @DeleteMapping("/rates")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeJobRate(@Parameter(description = "岗位ID") @RequestParam Long jobId,
                              @Parameter(description = "薪资规则ID") @RequestParam Long rateId) {
        jobService.removeJobRate(rateId);
    }

    @Operation(summary = "获取岗位排班", description = "根据岗位ID获取所有排班")
    @GetMapping("/schedules")
    public List<JobScheduleVO> getJobSchedules(@Parameter(description = "岗位ID") @RequestParam Long jobId) {
        return jobService.getJobSchedules(jobId);
    }

    @Operation(summary = "添加排班", description = "为岗位添加一条排班记录")
    @PostMapping("/schedules")
    @ResponseStatus(HttpStatus.CREATED)
    public JobScheduleVO addJobSchedule(@Parameter(description = "岗位ID") @RequestParam Long jobId, @RequestBody JobScheduleCmd request) {
        return jobService.addJobSchedule(jobId, request);
    }

    @Operation(summary = "更新排班", description = "更新岗位的排班记录")
    @PutMapping("/schedules")
    public JobScheduleVO updateJobSchedule(@Parameter(description = "岗位ID") @RequestParam Long jobId,
                                           @Parameter(description = "排班ID") @RequestParam Long scheduleId,
                                           @RequestBody JobScheduleCmd request) {
        return jobService.updateJobSchedule(scheduleId, request);
    }

    @Operation(summary = "删除排班", description = "删除岗位的排班记录")
    @DeleteMapping("/schedules")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeJobSchedule(@Parameter(description = "岗位ID") @RequestParam Long jobId,
                                  @Parameter(description = "排班ID") @RequestParam Long scheduleId) {
        jobService.removeJobSchedule(scheduleId);
    }
}
