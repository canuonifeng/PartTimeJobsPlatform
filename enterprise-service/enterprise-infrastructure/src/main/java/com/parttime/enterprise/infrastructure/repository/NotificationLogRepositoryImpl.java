package com.parttime.enterprise.infrastructure.repository;

import com.parttime.enterprise.core.domain.NotificationLog;
import com.parttime.enterprise.core.repository.NotificationLogRepository;
import com.parttime.enterprise.infrastructure.mapper.NotificationLogMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class NotificationLogRepositoryImpl implements NotificationLogRepository {

    private final NotificationLogMapper mapper;

    public NotificationLogRepositoryImpl(NotificationLogMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void save(NotificationLog log) {
        mapper.insert(log);
    }

    @Override
    public Optional<NotificationLog> findById(Long id) {
        return mapper.findById(id);
    }

    @Override
    public List<NotificationLog> findByRecipientIdAndRecipientType(Long recipientId, String recipientType) {
        return mapper.findByRecipientIdAndRecipientType(recipientId, recipientType);
    }

    @Override
    public void updateStatus(Long id, String status) {
        mapper.updateStatus(id, status);
    }

    @Override
    public void updateStatusWithError(Long id, String status, String errorMessage) {
        mapper.updateStatusWithError(id, status, errorMessage);
    }

    @Override
    public void markSent(Long id, LocalDateTime sentAt) {
        mapper.markSent(id, sentAt);
    }
}
