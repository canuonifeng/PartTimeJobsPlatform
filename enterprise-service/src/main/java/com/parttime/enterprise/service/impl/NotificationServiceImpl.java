package com.parttime.enterprise.service.impl;

import com.parttime.enterprise.dao.NotificationLogDao;
import com.parttime.enterprise.dao.NotificationTemplateDao;
import com.parttime.enterprise.pojo.cmd.NotificationTemplateCmd;
import com.parttime.enterprise.pojo.entity.NotificationLog;
import com.parttime.enterprise.pojo.entity.NotificationTemplate;
import com.parttime.enterprise.pojo.vo.NotificationLogVO;
import com.parttime.enterprise.service.NotificationService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationLogDao notificationLogDao;
    private final NotificationTemplateDao notificationTemplateDao;

    public NotificationServiceImpl(NotificationLogDao notificationLogDao,
                                    NotificationTemplateDao notificationTemplateDao) {
        this.notificationLogDao = notificationLogDao;
        this.notificationTemplateDao = notificationTemplateDao;
    }

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
        notificationLogDao.save(log);
        return toResponse(log);
    }

    @Override
    public List<NotificationLogVO> getNotificationsByRecipient(Long recipientId, String recipientType) {
        return notificationLogDao.findByRecipientIdAndRecipientType(recipientId, recipientType).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void markAsSent(Long id) {
        notificationLogDao.markSent(id, LocalDateTime.now());
    }

    @Override
    public void markAsFailed(Long id, String error) {
        notificationLogDao.updateStatusWithError(id, "FAILED", error);
    }

    @Override
    public List<NotificationTemplate> getTemplatesByType(String type) {
        return notificationTemplateDao.findByType(type);
    }

    @Override
    public List<NotificationTemplate> getNotificationTemplates(String type, String channel) {
        return notificationTemplateDao.findAll(type, channel);
    }

    @Override
    public NotificationTemplate createNotificationTemplate(NotificationTemplateCmd request) {
        NotificationTemplate template = new NotificationTemplate();
        template.setType(request.getType());
        template.setChannel(request.getChannel());
        template.setTitleTemplate(request.getTitleTemplate());
        template.setContentTemplate(request.getContentTemplate());
        notificationTemplateDao.save(template);
        return template;
    }

    @Override
    public NotificationTemplate updateNotificationTemplate(Long id, NotificationTemplateCmd request) {
        NotificationTemplate template = notificationTemplateDao.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification template not found: " + id));
        template.setTitleTemplate(request.getTitleTemplate());
        template.setContentTemplate(request.getContentTemplate());
        notificationTemplateDao.update(template);
        return template;
    }

    @Override
    public void deleteNotificationTemplate(Long id) {
        notificationTemplateDao.deleteById(id);
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
