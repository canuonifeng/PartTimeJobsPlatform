package com.parttime.enterprise.mapper;

import com.parttime.enterprise.pojo.entity.WorkerBalance;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface WorkerBalanceMapper {
    WorkerBalance findByWorkerId(Long workerId);
    List<WorkerBalance> findByWorkerIds(@Param("workerIds") List<Long> workerIds);
    int upsert(@Param("workerId") Long workerId,
               @Param("balance") java.math.BigDecimal balance,
               @Param("totalEarned") java.math.BigDecimal totalEarned,
               @Param("totalWithdrawn") java.math.BigDecimal totalWithdrawn);
}
