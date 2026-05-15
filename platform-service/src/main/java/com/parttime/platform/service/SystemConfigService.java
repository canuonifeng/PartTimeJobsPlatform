package com.parttime.platform.service;

import com.parttime.platform.pojo.vo.SystemConfigVO;

import java.util.List;

public interface SystemConfigService {

    String getConfig(String key);

    List<SystemConfigVO> getAllConfigs();

    SystemConfigVO updateConfig(String key, String value);
}
