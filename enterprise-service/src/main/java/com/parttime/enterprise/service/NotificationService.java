package com.parttime.enterprise.service;

import com.parttime.enterprise.pojo.cmd.NotificationTemplateCmd;
import com.parttime.enterprise.pojo.entity.NotificationTemplate;
import com.parttime.enterprise.pojo.vo.NotificationLogVO;

import java.util.List;

public interface NotificationService {

    NotificationLogVO sendNotification(Long recipientId, String recipientType, String type,
                                        String channel, String title, String content);

    List<NotificationLogVO> getNotificationsByRecipient(Long recipientId, String recipientType);

    void markAsSent(Long id);

    void markAsFailed(Long id, String error);

    List<NotificationTemplate> getTemplatesByType(String type);

    List<NotificationTemplate> getNotificationTemplates(String type, String channel);

    NotificationTemplate createNotificationTemplate(NotificationTemplateCmd request);

    NotificationTemplate updateNotificationTemplate(Long id, NotificationTemplateCmd request);

    void deleteNotificationTemplate(Long id);
}
