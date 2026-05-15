package com.parttime.platform.infrastructure.repository;

import com.parttime.platform.core.domain.SystemConfig;
import com.parttime.platform.core.repository.SystemConfigRepository;
import com.parttime.platform.infrastructure.mapper.SystemConfigMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class SystemConfigRepositoryImpl implements SystemConfigRepository {

    private final SystemConfigMapper mapper;

    public SystemConfigRepositoryImpl(SystemConfigMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Optional<SystemConfig> findByKey(String configKey) {
        return mapper.findByKey(configKey);
    }

    @Override
    public List<SystemConfig> findAll() {
        return mapper.findAll();
    }

    @Override
    public void updateByKey(String configKey, String configValue) {
        mapper.updateByKey(configKey, configValue);
    }
}
