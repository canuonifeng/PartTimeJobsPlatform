package com.parttime.cservice.mapper;

import com.parttime.cservice.pojo.entity.SystemConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;

@Mapper
public interface SystemConfigMapper {
    Optional<SystemConfig> findByKey(@Param("configKey") String configKey);
}
