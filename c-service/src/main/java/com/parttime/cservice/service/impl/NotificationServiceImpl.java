package com.parttime.cservice.service.impl;

import com.parttime.cservice.mapper.NotificationMapper;
import com.parttime.cservice.pojo.vo.NotificationVO;
import com.parttime.cservice.pojo.vo.PageVO;
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
        return createWorkerNotification(workerId, type, categoryOf(type), title, content, null, null);
    }

    public NotificationVO createWorkerNotification(Long workerId, String type, String category, String title, String content,
                                                   String relatedType, Long relatedId) {
        NotificationVO notification = new NotificationVO();
        notification.setRecipientId(workerId);
        notification.setRecipientType("WORKER");
        notification.setType(type);
        notification.setCategory(category == null ? categoryOf(type) : category);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setStatus("SENT");
        notification.setRead(false);
        notification.setRelatedType(relatedType);
        notification.setRelatedId(relatedId);
        notification.setSentAt(LocalDateTime.now());
        notificationMapper.insert(notification);
        return notification;
    }

    public PageVO<NotificationVO> getMyNotifications(Long workerId, Integer page, Integer pageSize) {
        int currentPage = page == null || page < 1 ? 1 : page;
        int currentPageSize = pageSize == null || pageSize < 1 ? 20 : Math.min(pageSize, 50);
        int offset = (currentPage - 1) * currentPageSize;
        List<NotificationVO> records = notificationMapper.findByRecipientId(workerId, "WORKER", offset, currentPageSize);
        long total = notificationMapper.countByRecipientId(workerId, "WORKER");
        return new PageVO<>(records, total);
    }

    public void markAsRead(Long workerId, Long notificationId) {
        if (notificationMapper.markAsRead(notificationId, workerId, "WORKER") == 0) {
            throw new RuntimeException("通知不存在");
        }
    }

    private String categoryOf(String type) {
        String value = String.valueOf(type == null ? "" : type).toUpperCase();
        if (value.contains("APPLICATION")) return "application";
        if (value.contains("SCHEDULE") || value.contains("SHIFT")) return "schedule";
        if (value.contains("EARNING") || value.contains("WITHDRAW") || value.contains("PAY")) return "finance";
        return "system";
    }
}
