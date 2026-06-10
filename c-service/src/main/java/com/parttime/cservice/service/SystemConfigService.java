package com.parttime.cservice.service;

import com.parttime.cservice.pojo.entity.SystemConfig;

import java.util.Optional;

public interface SystemConfigService {
    Optional<SystemConfig> findByKey(String key);
}
