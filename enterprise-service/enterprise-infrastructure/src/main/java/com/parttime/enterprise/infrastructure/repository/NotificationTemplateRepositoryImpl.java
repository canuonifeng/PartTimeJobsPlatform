package com.parttime.enterprise.infrastructure.repository;

import com.parttime.enterprise.core.domain.NotificationTemplate;
import com.parttime.enterprise.core.repository.NotificationTemplateRepository;
import com.parttime.enterprise.infrastructure.mapper.NotificationTemplateMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class NotificationTemplateRepositoryImpl implements NotificationTemplateRepository {

    private final NotificationTemplateMapper mapper;

    public NotificationTemplateRepositoryImpl(NotificationTemplateMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<NotificationTemplate> findByType(String type) {
        return mapper.findByType(type);
    }

    @Override
    public List<NotificationTemplate> findByTypeAndChannel(String type, String channel) {
        return mapper.findByTypeAndChannel(type, channel);
    }

    @Override
    public List<NotificationTemplate> findAll(String type, String channel) {
        return mapper.findAll(type, channel);
    }

    @Override
    public Optional<NotificationTemplate> findById(Long id) {
        return mapper.findById(id);
    }

    @Override
    public void save(NotificationTemplate template) {
        mapper.insert(template);
    }

    @Override
    public void update(NotificationTemplate template) {
        mapper.update(template);
    }

    @Override
    public void deleteById(Long id) {
        mapper.deleteById(id);
    }
}
