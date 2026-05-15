package com.parttime.enterprise.core.repository;

import com.parttime.enterprise.core.domain.NotificationLog;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface NotificationLogRepository {

    void save(NotificationLog log);

    Optional<NotificationLog> findById(Long id);

    List<NotificationLog> findByRecipientIdAndRecipientType(Long recipientId, String recipientType);

    void updateStatus(Long id, String status);

    void updateStatusWithError(Long id, String status, String errorMessage);

    void markSent(Long id, LocalDateTime sentAt);
}
