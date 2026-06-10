package com.parttime.cservice.service.impl;

import com.parttime.cservice.mapper.SystemConfigMapper;
import com.parttime.cservice.pojo.entity.SystemConfig;
import com.parttime.cservice.service.SystemConfigService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.Optional;

@Service
public class SystemConfigServiceImpl implements SystemConfigService {

    @Resource
    private SystemConfigMapper systemConfigMapper;

    @Override
    public Optional<SystemConfig> findByKey(String key) {
        return systemConfigMapper.findByKey(key);
    }
}
