package com.parttime.enterprise.mapper;

import com.parttime.enterprise.pojo.entity.EnterpriseBalance;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;

@Mapper
public interface EnterpriseBalanceMapper {

    EnterpriseBalance findByCompanyId(@Param("companyId") Long companyId);

    void upsert(@Param("companyId") Long companyId,
                @Param("balance") BigDecimal balance,
                @Param("totalTopUp") BigDecimal totalTopUp,
                @Param("totalSpent") BigDecimal totalSpent);
}
