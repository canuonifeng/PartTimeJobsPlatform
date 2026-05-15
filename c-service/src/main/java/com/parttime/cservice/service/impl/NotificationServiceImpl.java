package com.parttime.cservice.service.impl;

import com.parttime.cservice.mapper.NotificationMapper;
import com.parttime.cservice.pojo.vo.NotificationVO;
import com.parttime.cservice.service.NotificationService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {

    @Resource
    private NotificationMapper notificationMapper;

    public NotificationVO sendNotification(Long workerId, String type, String title, String content) {
        NotificationVO notification = new NotificationVO();
        notification.setRecipientId(workerId);
        notification.setRecipientType("WORKER");
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setStatus("PENDING");
        notification.setSentAt(LocalDateTime.now());
        notificationMapper.insert(notification);
        return notification;
    }

    public List<NotificationVO> getMyNotifications(Long workerId) {
        return notificationMapper.findByRecipientId(workerId, "WORKER");
    }
}
