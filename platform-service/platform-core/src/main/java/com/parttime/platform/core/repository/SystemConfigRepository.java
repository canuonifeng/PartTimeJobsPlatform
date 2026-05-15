package com.parttime.platform.core.repository;

import com.parttime.platform.core.domain.SystemConfig;

import java.util.List;
import java.util.Optional;

public interface SystemConfigRepository {

    Optional<SystemConfig> findByKey(String configKey);

    List<SystemConfig> findAll();

    void updateByKey(String configKey, String configValue);
}
