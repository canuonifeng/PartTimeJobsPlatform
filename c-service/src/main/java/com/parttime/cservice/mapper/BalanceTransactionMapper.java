package com.parttime.cservice.mapper;

import com.parttime.cservice.pojo.entity.BalanceTransaction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BalanceTransactionMapper {
    int insert(BalanceTransaction transaction);
    List<BalanceTransaction> findByWorkerId(@Param("workerId") Long workerId);
}
