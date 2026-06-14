package com.parttime.cservice.controller;

import com.parttime.cservice.pojo.vo.ApiResponse;
import com.parttime.cservice.service.HomeService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;

@RestController
@RequestMapping("/api/worker/home")
public class HomeController {

    @Resource
    private HomeService homeService;

    private Long getCurrentWorkerId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        return Long.valueOf(auth.getName());
    }

    @Operation(summary = "获取首页统计", description = "获取当前工人的首页统计数据")
    @GetMapping("/stats")
    public ApiResponse<?> getStats() {
        Long workerId = getCurrentWorkerId();
        if (workerId == null) {
            return ApiResponse.error(401, "未登录");
        }
        return ApiResponse.success(homeService.getStats(workerId));
    }

    @Operation(summary = "获取首页排班", description = "获取当前工人的今日和近期排班列表")
    @GetMapping("/schedules")
    public ApiResponse<?> getSchedules() {
        Long workerId = getCurrentWorkerId();
        if (workerId == null) {
            return ApiResponse.error(401, "未登录");
        }
        return ApiResponse.success(homeService.getSchedules(workerId));
    }
}
