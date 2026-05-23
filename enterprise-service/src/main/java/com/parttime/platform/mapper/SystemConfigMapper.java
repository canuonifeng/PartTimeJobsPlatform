package com.parttime.platform.mapper;

import com.parttime.platform.pojo.entity.SystemConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface SystemConfigMapper {

    Optional<SystemConfig> findByKey(String configKey);

    List<SystemConfig> findAll();

    int updateByKey(@Param("configKey") String configKey, @Param("configValue") String configValue);
}
