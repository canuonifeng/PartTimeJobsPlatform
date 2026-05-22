package com.parttime.cservice.mapper;

import com.parttime.cservice.pojo.entity.WorkerBalance;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;

@Mapper
public interface WorkerBalanceMapper {
    WorkerBalance findByWorkerId(Long workerId);
    int upsert(@Param("workerId") Long workerId,
               @Param("balance") BigDecimal balance,
               @Param("totalEarned") BigDecimal totalEarned,
               @Param("totalWithdrawn") BigDecimal totalWithdrawn);
}
