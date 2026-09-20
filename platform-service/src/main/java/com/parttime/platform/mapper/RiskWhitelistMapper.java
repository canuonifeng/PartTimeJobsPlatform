package com.parttime.platform.mapper;

import com.parttime.platform.pojo.entity.RiskWhitelist;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface RiskWhitelistMapper {

    List<RiskWhitelist> findByFilters(@Param("targetType") String targetType,
                                       @Param("status") String status,
                                       @Param("keyword") String keyword);

    Optional<RiskWhitelist> findById(@Param("id") Long id);

    int insert(RiskWhitelist record);

    int softRemove(@Param("id") Long id, @Param("operatorName") String operatorName);
}
