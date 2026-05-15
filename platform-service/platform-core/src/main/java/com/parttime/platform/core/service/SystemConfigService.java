package com.parttime.platform.core.service;

import com.parttime.platform.api.dto.SystemConfigResponse;
import com.parttime.platform.core.domain.SystemConfig;
import com.parttime.platform.core.exception.BusinessException;
import com.parttime.platform.core.repository.SystemConfigRepository;

import java.util.List;
import java.util.stream.Collectors;

public class SystemConfigService {

    private final SystemConfigRepository systemConfigRepository;

    public SystemConfigService(SystemConfigRepository systemConfigRepository) {
        this.systemConfigRepository = systemConfigRepository;
    }

    public String getConfig(String key) {
        return systemConfigRepository.findByKey(key)
                .map(SystemConfig::getConfigValue)
                .orElseThrow(() -> new BusinessException("Config not found: " + key));
    }

    public List<SystemConfigResponse> getAllConfigs() {
        return systemConfigRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public SystemConfigResponse updateConfig(String key, String value) {
        SystemConfig config = systemConfigRepository.findByKey(key)
                .orElseThrow(() -> new BusinessException("Config not found: " + key));
        systemConfigRepository.updateByKey(key, value);
        config.setConfigValue(value);
        return toResponse(config);
    }

    private SystemConfigResponse toResponse(SystemConfig config) {
        SystemConfigResponse response = new SystemConfigResponse();
        response.setId(config.getId());
        response.setConfigKey(config.getConfigKey());
        response.setConfigValue(config.getConfigValue());
        response.setDescription(config.getDescription());
        response.setCreatedAt(config.getCreatedAt());
        response.setUpdatedAt(config.getUpdatedAt());
        return response;
    }
}
