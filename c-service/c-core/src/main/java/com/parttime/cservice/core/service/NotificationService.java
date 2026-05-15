package com.parttime.cservice.core.service;

import com.parttime.cservice.core.dto.NotificationResponse;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private final ConcurrentHashMap<Long, ConcurrentHashMap<Long, NotificationResponse>> notifications = new ConcurrentHashMap<>();
    private final AtomicLong idCounter = new AtomicLong(1);

    public NotificationResponse sendNotification(Long workerId, String type, String title, String content) {
        NotificationResponse response = new NotificationResponse();
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

    public List<NotificationResponse> getMyNotifications(Long workerId) {
        ConcurrentHashMap<Long, NotificationResponse> userNotifications = notifications.get(workerId);
        if (userNotifications == null) {
            return List.of();
        }
        return userNotifications.values().stream()
                .sorted((a, b) -> b.getSentAt().compareTo(a.getSentAt()))
                .collect(Collectors.toList());
    }
}
