package com.parttime.cservice.service;

import com.parttime.cservice.pojo.vo.NotificationVO;

import java.util.List;

public interface NotificationService {
    NotificationVO sendNotification(Long workerId, String type, String title, String content);
    List<NotificationVO> getMyNotifications(Long workerId);
}
