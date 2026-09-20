package com.parttime.platform.mapper;

import com.parttime.platform.pojo.entity.RiskBlacklist;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface RiskBlacklistMapper {

    List<RiskBlacklist> findByFilters(@Param("targetType") String targetType,
                                      @Param("status") String status,
                                      @Param("keyword") String keyword);

    Optional<RiskBlacklist> findById(@Param("id") Long id);

    int insert(RiskBlacklist record);

    int softRemove(@Param("id") Long id,
                   @Param("reason") String reason,
                   @Param("operatorName") String operatorName);
}
