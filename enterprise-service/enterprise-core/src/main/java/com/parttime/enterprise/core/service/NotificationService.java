package com.parttime.enterprise.core.service;

import com.parttime.enterprise.api.dto.NotificationLogResponse;
import com.parttime.enterprise.api.dto.NotificationTemplateRequest;
import com.parttime.enterprise.core.domain.NotificationLog;
import com.parttime.enterprise.core.domain.NotificationTemplate;
import com.parttime.enterprise.core.repository.NotificationLogRepository;
import com.parttime.enterprise.core.repository.NotificationTemplateRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class NotificationService {

    private final NotificationLogRepository notificationLogRepository;
    private final NotificationTemplateRepository notificationTemplateRepository;

    public NotificationService(NotificationLogRepository notificationLogRepository,
                               NotificationTemplateRepository notificationTemplateRepository) {
        this.notificationLogRepository = notificationLogRepository;
        this.notificationTemplateRepository = notificationTemplateRepository;
    }

    public NotificationLogResponse sendNotification(Long recipientId, String recipientType, String type,
                                                     String channel, String title, String content) {
        NotificationLog log = new NotificationLog();
        log.setRecipientId(recipientId);
        log.setRecipientType(recipientType);
        log.setType(type);
        log.setChannel(channel);
        log.setTitle(title);
        log.setContent(content);
        log.setStatus("PENDING");
        notificationLogRepository.save(log);
        return toResponse(log);
    }

    public List<NotificationLogResponse> getNotificationsByRecipient(Long recipientId, String recipientType) {
        return notificationLogRepository.findByRecipientIdAndRecipientType(recipientId, recipientType).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public void markAsSent(Long id) {
        notificationLogRepository.markSent(id, LocalDateTime.now());
    }

    public void markAsFailed(Long id, String error) {
        notificationLogRepository.updateStatusWithError(id, "FAILED", error);
    }

    public List<NotificationTemplate> getTemplatesByType(String type) {
        return notificationTemplateRepository.findByType(type);
    }

    public List<NotificationTemplate> getNotificationTemplates(String type, String channel) {
        return notificationTemplateRepository.findAll(type, channel);
    }

    public NotificationTemplate createNotificationTemplate(NotificationTemplateRequest request) {
        NotificationTemplate template = new NotificationTemplate();
        template.setType(request.getType());
        template.setChannel(request.getChannel());
        template.setTitleTemplate(request.getTitleTemplate());
        template.setContentTemplate(request.getContentTemplate());
        notificationTemplateRepository.save(template);
        return template;
    }

    public NotificationTemplate updateNotificationTemplate(Long id, NotificationTemplateRequest request) {
        NotificationTemplate template = notificationTemplateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification template not found: " + id));
        template.setTitleTemplate(request.getTitleTemplate());
        template.setContentTemplate(request.getContentTemplate());
        notificationTemplateRepository.update(template);
        return template;
    }

    public void deleteNotificationTemplate(Long id) {
        notificationTemplateRepository.deleteById(id);
    }

    private NotificationLogResponse toResponse(NotificationLog log) {
        NotificationLogResponse response = new NotificationLogResponse();
        response.setId(log.getId());
        response.setRecipientId(log.getRecipientId());
        response.setRecipientType(log.getRecipientType());
        response.setType(log.getType());
        response.setChannel(log.getChannel());
        response.setTitle(log.getTitle());
        response.setContent(log.getContent());
        response.setStatus(log.getStatus());
        response.setErrorMessage(log.getErrorMessage());
        response.setSentAt(log.getSentAt());
        response.setCreatedAt(log.getCreatedAt());
        return response;
    }
}
