package com.parttime.enterprise.controller;

import com.parttime.enterprise.pojo.cmd.IdCmd;
import com.parttime.enterprise.config.SecurityUtil;
import com.parttime.enterprise.pojo.cmd.JobCreateCmd;
import com.parttime.enterprise.pojo.cmd.JobRateCmd;
import com.parttime.enterprise.pojo.cmd.JobScheduleCmd;
import com.parttime.enterprise.pojo.cmd.UpdateJobCmd;
import com.parttime.enterprise.pojo.vo.ApiResponse;
import com.parttime.enterprise.pojo.vo.JobRateVO;
import com.parttime.enterprise.pojo.vo.JobScheduleVO;
import com.parttime.enterprise.pojo.vo.JobVO;
import com.parttime.enterprise.service.JobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
    public ApiResponse<List<JobVO>> listJobs(@RequestParam(required = false) String status) {
        Long companyId = SecurityUtil.getCurrentCompanyId();
        return ApiResponse.success(jobService.getJobsByCompany(companyId, status));
    }

    @Operation(summary = "获取岗位详情")
    @GetMapping(params = "id")
    public ApiResponse<JobVO> getJob(@RequestParam Long id) {
        return ApiResponse.success(jobService.getJobById(id));
    }

    @Operation(summary = "创建岗位")
    @PostMapping
    public ApiResponse<JobVO> createJob(@RequestBody JobCreateCmd request) {
        Long companyId = SecurityUtil.getCurrentCompanyId();
        request.setCompanyId(companyId);
        return ApiResponse.success(jobService.createJob(request));
    }

    @Operation(summary = "更新岗位")
    @PostMapping("/update")
    public ApiResponse<JobVO> updateJob(@RequestBody UpdateJobCmd request) {
        return ApiResponse.success(jobService.updateJob(request.getId(), request));
    }

    @Operation(summary = "删除岗位")
    @PostMapping("/delete")
    public void deleteJob(@RequestBody IdCmd cmd) {
        jobService.deleteJob(cmd.getId());
    }

    @Operation(summary = "发布岗位", description = "将草稿状态的岗位发布为已发布状态")
    @PostMapping("/publish")
    public ApiResponse<JobVO> publishJob(@RequestBody IdCmd cmd) {
        return ApiResponse.success(jobService.publishJob(cmd.getId()));
    }

    @Operation(summary = "关闭岗位", description = "关闭已发布的岗位")
    @PostMapping("/close")
    public ApiResponse<JobVO> closeJob(@RequestBody IdCmd cmd) {
        return ApiResponse.success(jobService.closeJob(cmd.getId()));
    }

    @Operation(summary = "重新开放岗位", description = "重新开放已关闭的岗位")
    @PostMapping("/reopen")
    public ApiResponse<JobVO> reopenJob(@RequestBody IdCmd cmd) {
        return ApiResponse.success(jobService.reopenJob(cmd.getId()));
    }

    @Operation(summary = "获取岗位薪资规则", description = "根据岗位ID获取所有薪资规则")
    @GetMapping("/rates")
    public ApiResponse<List<JobRateVO>> getJobRates(@Parameter(description = "岗位ID") @RequestParam Long jobId) {
        return ApiResponse.success(jobService.getJobRates(jobId));
    }

    @Operation(summary = "添加薪资规则", description = "为岗位添加一条薪资规则")
    @PostMapping("/rates")
    public ApiResponse<JobRateVO> addJobRate(@RequestBody JobRateCmd request) {
        return ApiResponse.success(jobService.addJobRate(request.getJobId(), request));
    }

    @Operation(summary = "更新薪资规则", description = "更新岗位的薪资规则")
    @PostMapping("/rates/update")
    public ApiResponse<JobRateVO> updateJobRate(@RequestBody JobRateCmd request) {
        return ApiResponse.success(jobService.updateJobRate(request.getRateId(), request));
    }

    @Operation(summary = "删除薪资规则", description = "删除岗位的薪资规则")
    @PostMapping("/rates/delete")
    public void removeJobRate(@RequestBody JobRateCmd request) {
        jobService.removeJobRate(request.getRateId());
    }

    @Operation(summary = "获取岗位排班", description = "根据岗位ID获取所有排班")
    @GetMapping("/schedules")
    public ApiResponse<List<JobScheduleVO>> getJobSchedules(@Parameter(description = "岗位ID") @RequestParam Long jobId) {
        return ApiResponse.success(jobService.getJobSchedules(jobId));
    }

    @Operation(summary = "添加排班", description = "为岗位添加一条排班记录")
    @PostMapping("/schedules")
    public ApiResponse<JobScheduleVO> addJobSchedule(@RequestBody JobScheduleCmd request) {
        return ApiResponse.success(jobService.addJobSchedule(request.getJobId(), request));
    }

    @Operation(summary = "更新排班", description = "更新岗位的排班记录")
    @PostMapping("/schedules/update")
    public ApiResponse<JobScheduleVO> updateJobSchedule(@RequestBody JobScheduleCmd request) {
        return ApiResponse.success(jobService.updateJobSchedule(request.getId(), request));
    }

    @Operation(summary = "删除排班", description = "删除岗位的排班记录")
    @PostMapping("/schedules/delete")
    public void removeJobSchedule(@RequestBody JobScheduleCmd request) {
        jobService.removeJobSchedule(request.getId());
    }
}
