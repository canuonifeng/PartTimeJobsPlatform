package com.parttime.enterprise.mapper;

import com.parttime.enterprise.pojo.entity.BalanceTransaction;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BalanceTransactionMapper {
    int insert(BalanceTransaction transaction);
}
