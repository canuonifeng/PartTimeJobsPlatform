package com.parttime.cservice.service;

import com.parttime.cservice.pojo.vo.NotificationVO;
import com.parttime.cservice.pojo.vo.PageVO;

public interface NotificationService {
    NotificationVO sendNotification(Long workerId, String type, String title, String content);
    NotificationVO createWorkerNotification(Long workerId, String type, String category, String title, String content, String relatedType, Long relatedId);
    PageVO<NotificationVO> getMyNotifications(Long workerId, Integer page, Integer pageSize);
    void markAsRead(Long workerId, Long notificationId);
}
