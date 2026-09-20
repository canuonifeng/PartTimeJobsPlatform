package com.parttime.platform.mapper;

import com.parttime.platform.pojo.entity.RiskRule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface RiskRuleMapper {

    List<RiskRule> findAll();

    Optional<RiskRule> findById(@Param("id") Long id);

    Optional<RiskRule> findByCode(@Param("ruleCode") String ruleCode);

    int insert(RiskRule rule);

    int update(RiskRule rule);

    int updateStatus(@Param("id") Long id, @Param("status") String status);
}
