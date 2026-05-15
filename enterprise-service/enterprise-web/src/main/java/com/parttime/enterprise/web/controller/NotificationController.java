package com.parttime.enterprise.web.controller;

import com.parttime.enterprise.api.dto.NotificationLogResponse;
import com.parttime.enterprise.api.dto.NotificationTemplateRequest;
import com.parttime.enterprise.core.domain.NotificationTemplate;
import com.parttime.enterprise.core.service.NotificationService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/notifications")
    public List<NotificationLogResponse> getNotifications(
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
    public ResponseEntity<NotificationTemplate> createTemplate(@RequestBody NotificationTemplateRequest request) {
        NotificationTemplate template = notificationService.createNotificationTemplate(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(template);
    }

    @PutMapping("/notification-templates/{id}")
    public ResponseEntity<NotificationTemplate> updateTemplate(
            @PathVariable Long id,
            @RequestBody NotificationTemplateRequest request) {
        try {
            NotificationTemplate template = notificationService.updateNotificationTemplate(id, request);
            return ResponseEntity.ok(template);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/notification-templates/{id}")
    public ResponseEntity<Void> deleteTemplate(@PathVariable Long id) {
        notificationService.deleteNotificationTemplate(id);
        return ResponseEntity.noContent().build();
    }
}
