package com.parttime.enterprise.controller;

import com.parttime.enterprise.pojo.cmd.NotificationTemplateCmd;
import com.parttime.enterprise.pojo.entity.NotificationTemplate;
import com.parttime.enterprise.pojo.vo.NotificationLogVO;
import com.parttime.enterprise.service.NotificationService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

import java.util.List;

@RestController
@RequestMapping("/api")
public class NotificationController {

    @Resource
    private NotificationService notificationService;

    @GetMapping("/notifications")
    public List<NotificationLogVO> getNotifications(
            @RequestParam Long recipientId,
            @RequestParam(defaultValue = "ENTERPRISE") String recipientType) {
        return notificationService.getNotificationsByRecipient(recipientId, recipientType);
    }

    @GetMapping("/notification-templates")
    public List<NotificationTemplate> getTemplates(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String channel) {
        return notificationService.getNotificationTemplates(type, channel);
    }

    @PostMapping("/notification-templates")
    public ResponseEntity<NotificationTemplate> createTemplate(@RequestBody NotificationTemplateCmd request) {
        NotificationTemplate template = notificationService.createNotificationTemplate(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(template);
    }

    @PutMapping("/notification-templates")
    public ResponseEntity<NotificationTemplate> updateTemplate(
            @RequestParam Long id,
            @RequestBody NotificationTemplateCmd request) {
        try {
            NotificationTemplate template = notificationService.updateNotificationTemplate(id, request);
            return ResponseEntity.ok(template);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/notification-templates")
    public ResponseEntity<Void> deleteTemplate(@RequestParam Long id) {
        notificationService.deleteNotificationTemplate(id);
        return ResponseEntity.noContent().build();
    }
}
