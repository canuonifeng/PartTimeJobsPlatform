package com.parttime.cservice.mapper;

import com.parttime.cservice.pojo.entity.BalanceTransaction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BalanceTransactionMapper {
    int insert(BalanceTransaction transaction);
    List<BalanceTransaction> findByWorkerId(@Param("workerId") Long workerId);
    List<BalanceTransaction> findByWorkerIdPage(@Param("workerId") Long workerId,
                                                  @Param("offset") int offset,
                                                  @Param("pageSize") int pageSize);
    long countByWorkerId(@Param("workerId") Long workerId);
}
