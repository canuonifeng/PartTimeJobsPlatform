package com.parttime.enterprise.controller;

import com.parttime.enterprise.pojo.cmd.IdCmd;
import com.parttime.enterprise.pojo.cmd.NotificationTemplateCmd;
import com.parttime.enterprise.pojo.entity.NotificationTemplate;
import com.parttime.enterprise.pojo.vo.NotificationLogVO;
import com.parttime.enterprise.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

import com.parttime.enterprise.pojo.vo.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

import java.util.List;

@RestController
@RequestMapping("/api/enterprise")
public class NotificationController {

    @Resource
    private NotificationService notificationService;

    @Operation(summary = "获取通知列表", description = "根据接收者ID和类型获取通知列表")
    @GetMapping("/notifications")
    public List<NotificationLogVO> getNotifications(
            @Parameter(description = "接收者ID") @RequestParam Long recipientId,
            @Parameter(description = "接收者类型: ENTERPRISE-企业, WORKER-工人") @RequestParam(defaultValue = "ENTERPRISE") String recipientType) {
        return notificationService.getNotificationsByRecipient(recipientId, recipientType);
    }

    @Operation(summary = "获取通知模板列表", description = "根据类型和渠道筛选通知模板")
    @GetMapping("/notification-templates")
    public List<NotificationTemplate> getTemplates(
            @Parameter(description = "通知类型") @RequestParam(required = false) String type,
            @Parameter(description = "发送渠道") @RequestParam(required = false) String channel) {
        return notificationService.getNotificationTemplates(type, channel);
    }

    @Operation(summary = "创建通知模板", description = "创建新的通知模板")
    @PostMapping("/notification-templates")
    public ApiResponse<NotificationTemplate> createTemplate(@RequestBody NotificationTemplateCmd request) {
        NotificationTemplate template = notificationService.createNotificationTemplate(request);
        return ApiResponse.success(template);
    }

    @Operation(summary = "更新通知模板", description = "更新指定的通知模板")
    @PostMapping("/notification-templates/update")
    public ApiResponse<NotificationTemplate> updateTemplate(@RequestBody NotificationTemplateCmd request) {
        try {
            NotificationTemplate template = notificationService.updateNotificationTemplate(request.getId(), request);
            return ApiResponse.success(template);
        } catch (RuntimeException e) {
            return ApiResponse.error("模板不存在");
        }
    }

    @Operation(summary = "删除通知模板", description = "删除指定的通知模板")
    @PostMapping("/notification-templates/delete")
    public ApiResponse<Void> deleteTemplate(@RequestBody IdCmd cmd) {
        notificationService.deleteNotificationTemplate(cmd.getId());
        return ApiResponse.success();
    }
}
