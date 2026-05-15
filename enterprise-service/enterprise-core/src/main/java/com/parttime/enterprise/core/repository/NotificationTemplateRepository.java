package com.parttime.enterprise.core.repository;

import com.parttime.enterprise.core.domain.NotificationTemplate;

import java.util.List;
import java.util.Optional;

public interface NotificationTemplateRepository {

    List<NotificationTemplate> findByType(String type);

    List<NotificationTemplate> findByTypeAndChannel(String type, String channel);

    List<NotificationTemplate> findAll(String type, String channel);

    Optional<NotificationTemplate> findById(Long id);

    void save(NotificationTemplate template);

    void update(NotificationTemplate template);

    void deleteById(Long id);
}
