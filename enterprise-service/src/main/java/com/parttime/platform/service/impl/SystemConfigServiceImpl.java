package com.parttime.platform.service.impl;

import com.parttime.platform.exception.BusinessException;
import com.parttime.platform.mapper.SystemConfigMapper;
import com.parttime.platform.pojo.entity.SystemConfig;
import com.parttime.platform.pojo.vo.SystemConfigVO;
import com.parttime.platform.service.SystemConfigService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SystemConfigServiceImpl implements SystemConfigService {

    @Resource
    private SystemConfigMapper systemConfigMapper;

    @Override
    public String getConfig(String key) {
        return systemConfigMapper.findByKey(key)
                .map(SystemConfig::getConfigValue)
                .orElseThrow(() -> new BusinessException("Config not found: " + key));
    }

    @Override
    public List<SystemConfigVO> getAllConfigs() {
        return systemConfigMapper.findAll().stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    @Override
    public SystemConfigVO updateConfig(String key, String value) {
        SystemConfig config = systemConfigMapper.findByKey(key)
                .orElseThrow(() -> new BusinessException("Config not found: " + key));
        systemConfigMapper.updateByKey(key, value);
        config.setConfigValue(value);
        return toVO(config);
    }

    private SystemConfigVO toVO(SystemConfig config) {
        SystemConfigVO vo = new SystemConfigVO();
        vo.setId(config.getId());
        vo.setConfigKey(config.getConfigKey());
        vo.setConfigValue(config.getConfigValue());
        vo.setDescription(config.getDescription());
        vo.setCreatedAt(config.getCreatedAt());
        vo.setUpdatedAt(config.getUpdatedAt());
        return vo;
    }
}
