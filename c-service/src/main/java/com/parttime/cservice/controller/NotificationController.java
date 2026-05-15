package com.parttime.cservice.controller;

import com.parttime.cservice.pojo.vo.NotificationVO;
import com.parttime.cservice.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Resource
    private NotificationService notificationService;

    @Operation(summary = "获取我的通知", description = "获取当前工人的通知列表")
    @GetMapping("/my")
    public ResponseEntity<List<NotificationVO>> getMyNotifications() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Long workerId = Long.valueOf(auth.getName());
        List<NotificationVO> notifications = notificationService.getMyNotifications(workerId);
        return ResponseEntity.ok(notifications);
    }
}
