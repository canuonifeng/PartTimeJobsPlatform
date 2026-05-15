package com.parttime.cservice.service.impl;

import com.parttime.cservice.pojo.vo.NotificationVO;
import com.parttime.cservice.service.NotificationService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final ConcurrentHashMap<Long, ConcurrentHashMap<Long, NotificationVO>> notifications = new ConcurrentHashMap<>();
    private final AtomicLong idCounter = new AtomicLong(1);

    public NotificationVO sendNotification(Long workerId, String type, String title, String content) {
        NotificationVO response = new NotificationVO();
        response.setId(idCounter.getAndIncrement());
        response.setType(type);
        response.setTitle(title);
        response.setContent(content);
        response.setStatus("PENDING");
        response.setSentAt(LocalDateTime.now());

        notifications.computeIfAbsent(workerId, k -> new ConcurrentHashMap<>())
                .put(response.getId(), response);

        return response;
    }

    public List<NotificationVO> getMyNotifications(Long workerId) {
        ConcurrentHashMap<Long, NotificationVO> userNotifications = notifications.get(workerId);
        if (userNotifications == null) {
            return List.of();
        }
        return userNotifications.values().stream()
                .sorted((a, b) -> b.getSentAt().compareTo(a.getSentAt()))
                .collect(Collectors.toList());
    }
}
