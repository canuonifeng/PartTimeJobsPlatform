package com.parttime.cservice.mapper;

import com.parttime.cservice.pojo.entity.ReferralConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ReferralConfigMapper {

    List<ReferralConfig> findAll();

    ReferralConfig findByKey(@Param("configKey") String configKey);

    void upsert(@Param("configKey") String configKey,
                @Param("configValue") String configValue,
                @Param("description") String description);
}
