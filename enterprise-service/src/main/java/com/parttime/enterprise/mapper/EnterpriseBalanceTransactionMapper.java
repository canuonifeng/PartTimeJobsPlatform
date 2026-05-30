package com.parttime.enterprise.mapper;

import com.parttime.enterprise.pojo.entity.EnterpriseBalanceTransaction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface EnterpriseBalanceTransactionMapper {

    void insert(EnterpriseBalanceTransaction transaction);

    List<EnterpriseBalanceTransaction> findByCompanyIdPage(@Param("companyId") Long companyId,
                                                            @Param("offset") int offset,
                                                            @Param("pageSize") int pageSize);

    long countByCompanyId(@Param("companyId") Long companyId);
}
