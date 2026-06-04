package com.parttime.cservice.controller;

import com.parttime.cservice.service.HomeService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;

@RestController
@RequestMapping("/api/home")
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
    public ResponseEntity<?> getStats() {
        Long workerId = getCurrentWorkerId();
        if (workerId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(homeService.getStats(workerId));
    }

    @Operation(summary = "获取首页排班", description = "获取当前工人的今日和近期排班列表")
    @GetMapping("/schedules")
    public ResponseEntity<?> getSchedules() {
        Long workerId = getCurrentWorkerId();
        if (workerId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(homeService.getSchedules(workerId));
    }
}
