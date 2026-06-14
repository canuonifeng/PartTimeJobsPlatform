package com.parttime.cservice.controller;

import com.parttime.cservice.pojo.vo.ApiResponse;
import com.parttime.cservice.pojo.vo.ProfileCompletenessVO;
import com.parttime.cservice.pojo.vo.ProfileDashboardVO;
import com.parttime.cservice.pojo.vo.ProfileVO;
import com.parttime.cservice.pojo.cmd.ProfileUpdateCmd;
import com.parttime.cservice.pojo.vo.ResumeVO;
import com.parttime.cservice.pojo.cmd.ResumeUploadCmd;
import com.parttime.cservice.service.HomeService;
import com.parttime.cservice.service.ProfileService;
import com.parttime.cservice.service.WithdrawalService;
import com.parttime.cservice.service.WorkerRealNameAuthService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/worker")
public class ProfileController {

    @Resource
    private ProfileService profileService;
    @Resource
    private HomeService homeService;
    @Resource
    private WithdrawalService withdrawalService;
    @Resource
    private WorkerRealNameAuthService workerRealNameAuthService;

    private Long getCurrentWorkerId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        return Long.valueOf(auth.getName());
    }

    @Operation(summary = "获取工人档案", description = "获取当前登录工人的详细档案信息")
    @GetMapping("/profile")
    public ApiResponse<ProfileVO> getProfile() {
        Long workerId = getCurrentWorkerId();
        if (workerId == null) {
            return ApiResponse.error(401, "未登录");
        }
        try {
            ProfileVO response = profileService.getProfile(workerId);
            return ApiResponse.success(response);
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @Operation(summary = "获取我的页聚合数据", description = "获取当前登录工人的档案、统计和收入汇总")
    @GetMapping("/profile/dashboard")
    public ApiResponse<ProfileDashboardVO> getDashboard() {
        Long workerId = getCurrentWorkerId();
        if (workerId == null) {
            return ApiResponse.error(401, "未登录");
        }
        ProfileDashboardVO response = new ProfileDashboardVO();
        response.setProfile(profileService.getProfile(workerId));
        response.setStats(homeService.getStats(workerId));
        response.setEarningsSummary(withdrawalService.getEarningsSummary(workerId));
        response.setRealNameAuth(workerRealNameAuthService.getStatus(workerId));
        return ApiResponse.success(response);
    }

    @Operation(summary = "更新工人档案", description = "更新当前登录工人的档案信息")
    @PostMapping("/profile")
    public ApiResponse<ProfileVO> updateProfile(@RequestBody ProfileUpdateCmd request) {
        Long workerId = getCurrentWorkerId();
        if (workerId == null) {
            return ApiResponse.error(401, "未登录");
        }
        ProfileVO response = profileService.updateProfile(workerId, request);
        return ApiResponse.success(response);
    }

    @Operation(summary = "检查档案完整性", description = "返回当前工人的基本信息是否完整及缺失字段")
    @GetMapping("/profile/completeness")
    public ApiResponse<ProfileCompletenessVO> getCompleteness() {
        Long workerId = getCurrentWorkerId();
        if (workerId == null) {
            return ApiResponse.error(401, "未登录");
        }
        ProfileCompletenessVO response = profileService.getCompleteness(workerId);
        return ApiResponse.success(response);
    }

    @Operation(summary = "上传简历", description = "工人上传简历文件")
    @PostMapping("/profile/resumes")
    public ApiResponse<ResumeVO> uploadResume(@RequestBody ResumeUploadCmd request) {
        Long workerId = getCurrentWorkerId();
        if (workerId == null) {
            return ApiResponse.error(401, "未登录");
        }
        ResumeVO response = profileService.uploadResume(workerId, request.getFileName(), request.getFileUrl());
        return ApiResponse.success(response);
    }

    @Operation(summary = "获取简历列表", description = "获取当前登录工人的简历列表")
    @GetMapping("/profile/resumes")
    public ApiResponse<List<ResumeVO>> getResumes() {
        Long workerId = getCurrentWorkerId();
        if (workerId == null) {
            return ApiResponse.error(401, "未登录");
        }
        List<ResumeVO> responses = profileService.getResumes(workerId);
        return ApiResponse.success(responses);
    }
}
