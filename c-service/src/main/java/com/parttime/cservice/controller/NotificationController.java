package com.parttime.cservice.controller;

import com.parttime.cservice.pojo.vo.ApiResponse;
import com.parttime.cservice.pojo.vo.NotificationVO;
import com.parttime.cservice.pojo.vo.PageVO;
import com.parttime.cservice.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Resource
    private NotificationService notificationService;

    @Operation(summary = "获取我的通知", description = "获取当前工人的通知列表")
    @GetMapping("/my")
    public ApiResponse<PageVO<NotificationVO>> getMyNotifications(@RequestParam(defaultValue = "1") Integer page,
                                                                       @RequestParam(defaultValue = "20") Integer pageSize) {
        Long workerId = currentWorkerId();
        if (workerId == null) {
            return ApiResponse.error(401, "未登录");
        }
        return ApiResponse.success(notificationService.getMyNotifications(workerId, page, pageSize));
    }

    @Operation(summary = "标记通知已读", description = "将当前工人的指定通知标记为已读")
    @PostMapping("/read")
    public ApiResponse<Void> markAsRead(@RequestBody com.parttime.cservice.pojo.cmd.IdCmd cmd) {
        Long workerId = currentWorkerId();
        if (workerId == null) {
            return ApiResponse.error(401, "未登录");
        }
        notificationService.markAsRead(workerId, cmd.getId());
        return ApiResponse.success();
    }

    private Long currentWorkerId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        return Long.valueOf(auth.getName());
    }
}
