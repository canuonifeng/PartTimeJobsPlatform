package com.parttime.cservice.controller;

import com.parttime.cservice.pojo.cmd.ApplyJobCmd;
import com.parttime.cservice.pojo.entity.ScheduleApplication;
import com.parttime.cservice.pojo.vo.ApiResponse;
import com.parttime.cservice.pojo.vo.JobDetailVO;
import com.parttime.cservice.pojo.vo.JobSummaryVO;
import com.parttime.cservice.pojo.vo.PageVO;
import com.parttime.cservice.pojo.vo.ProfileCompletenessVO;
import com.parttime.cservice.pojo.vo.WorkerSignupVO;
import com.parttime.cservice.service.JobService;
import com.parttime.cservice.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/worker/jobs")
public class JobController {

    @Resource
    private JobService jobService;

    @Resource
    private ProfileService profileService;

    @Operation(summary = "搜索岗位", description = "根据关键词、分类、地点和薪资范围搜索岗位")
    @GetMapping
    public ApiResponse<List<JobSummaryVO>> searchJobs(
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "岗位分类ID") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "工作地点") @RequestParam(required = false) String location,
            @Parameter(description = "最低薪资") @RequestParam(required = false) BigDecimal minRate,
            @Parameter(description = "最高薪资") @RequestParam(required = false) BigDecimal maxRate,
            @Parameter(description = "当前位置纬度") @RequestParam(required = false) BigDecimal latitude,
            @Parameter(description = "当前位置经度") @RequestParam(required = false) BigDecimal longitude) {
        log.info("job");
        List<JobSummaryVO> results = jobService.searchJobs(keyword, categoryId, location, minRate, maxRate, latitude, longitude);
        return ApiResponse.success(results);
    }

    @Operation(summary = "获取岗位详情", description = "根据ID获取岗位详细信息，包括薪资规则和排班")
    @GetMapping("/detail")
    public ApiResponse<JobDetailVO> getJobDetail(@Parameter(description = "岗位ID") @RequestParam Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long workerId = null;
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() != null && !"anonymousUser".equals(auth.getPrincipal())) {
            workerId = Long.valueOf(auth.getName());
        }
        JobDetailVO detail = jobService.getJobDetail(id, workerId);
        return ApiResponse.success(detail);
    }

    @Operation(summary = "申请岗位", description = "工人申请岗位并选择排班")
    @PostMapping("/apply")
    public ApiResponse<?> applyForJob(@RequestBody ApplyJobCmd request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return ApiResponse.error(401, "未登录");
        }
        Long workerId = Long.valueOf(auth.getName());
        ProfileCompletenessVO completeness = profileService.getCompleteness(workerId);
        if (!completeness.isComplete()) {
            return ApiResponse.error("PROFILE_INCOMPLETE，请先完善个人信息");
        }
        boolean success = jobService.applyForJob(workerId, request.jobId(), request.scheduleIds());
        return ApiResponse.success(Map.of("success", success));
    }

    @Operation(summary = "获取我的报名", description = "获取当前工人的报名记录")
    @GetMapping("/applications/my")
    public ApiResponse<PageVO<WorkerSignupVO>> getMySignups(@RequestParam(defaultValue = "1") Integer page,
                                                                @RequestParam(defaultValue = "10") Integer pageSize) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return ApiResponse.error(401, "未登录");
        }
        Long workerId = Long.valueOf(auth.getName());
        return ApiResponse.success(jobService.getMySignups(workerId, page, pageSize));
    }

    @Operation(summary = "获取申请状态", description = "获取工人在指定岗位的申请状态")
    @GetMapping("/application")
    public ApiResponse<List<ScheduleApplication>> getApplicationStatus(@Parameter(description = "岗位ID") @RequestParam Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return ApiResponse.error(401, "未登录");
        }
        Long workerId = Long.valueOf(auth.getName());
        List<ScheduleApplication> statuses = jobService.getApplicationStatus(workerId, id);
        return ApiResponse.success(statuses);
    }
}
