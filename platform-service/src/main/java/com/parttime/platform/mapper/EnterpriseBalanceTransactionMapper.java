package com.parttime.platform.mapper;

import com.parttime.platform.pojo.entity.EnterpriseBalanceTransaction;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface EnterpriseBalanceTransactionMapper {

    void insert(EnterpriseBalanceTransaction transaction);
}
