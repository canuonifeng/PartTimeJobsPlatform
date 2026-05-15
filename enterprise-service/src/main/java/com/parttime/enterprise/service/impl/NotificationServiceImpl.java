package com.parttime.enterprise.service.impl;

import com.parttime.enterprise.mapper.NotificationLogMapper;
import com.parttime.enterprise.mapper.NotificationTemplateMapper;
import com.parttime.enterprise.pojo.cmd.NotificationTemplateCmd;
import com.parttime.enterprise.pojo.entity.NotificationLog;
import com.parttime.enterprise.pojo.entity.NotificationTemplate;
import com.parttime.enterprise.pojo.vo.NotificationLogVO;
import com.parttime.enterprise.service.NotificationService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationServiceImpl implements NotificationService {

    @Resource
    private NotificationLogMapper notificationLogMapper;
    @Resource
    private NotificationTemplateMapper notificationTemplateMapper;

    @Override
    public NotificationLogVO sendNotification(Long recipientId, String recipientType, String type,
                                                String channel, String title, String content) {
        NotificationLog log = new NotificationLog();
        log.setRecipientId(recipientId);
        log.setRecipientType(recipientType);
        log.setType(type);
        log.setChannel(channel);
        log.setTitle(title);
        log.setContent(content);
        log.setStatus("PENDING");
        notificationLogMapper.insert(log);
        return toResponse(log);
    }

    @Override
    public List<NotificationLogVO> getNotificationsByRecipient(Long recipientId, String recipientType) {
        return notificationLogMapper.findByRecipientIdAndRecipientType(recipientId, recipientType).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void markAsSent(Long id) {
        notificationLogMapper.markSent(id, LocalDateTime.now());
    }

    @Override
    public void markAsFailed(Long id, String error) {
        notificationLogMapper.updateStatusWithError(id, "FAILED", error);
    }

    @Override
    public List<NotificationTemplate> getTemplatesByType(String type) {
        return notificationTemplateMapper.findByType(type);
    }

    @Override
    public List<NotificationTemplate> getNotificationTemplates(String type, String channel) {
        return notificationTemplateMapper.findAll(type, channel);
    }

    @Override
    public NotificationTemplate createNotificationTemplate(NotificationTemplateCmd request) {
        NotificationTemplate template = new NotificationTemplate();
        template.setType(request.getType());
        template.setChannel(request.getChannel());
        template.setTitleTemplate(request.getTitleTemplate());
        template.setContentTemplate(request.getContentTemplate());
        notificationTemplateMapper.insert(template);
        return template;
    }

    @Override
    public NotificationTemplate updateNotificationTemplate(Long id, NotificationTemplateCmd request) {
        NotificationTemplate template = notificationTemplateMapper.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification template not found: " + id));
        template.setTitleTemplate(request.getTitleTemplate());
        template.setContentTemplate(request.getContentTemplate());
        notificationTemplateMapper.update(template);
        return template;
    }

    @Override
    public void deleteNotificationTemplate(Long id) {
        notificationTemplateMapper.deleteById(id);
    }

    private NotificationLogVO toResponse(NotificationLog log) {
        NotificationLogVO response = new NotificationLogVO();
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
