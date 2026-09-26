package com.parttime.cservice.mapper;

import com.parttime.cservice.pojo.entity.BalanceTransaction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface BalanceTransactionMapper {
    int insert(BalanceTransaction transaction);
    List<BalanceTransaction> findByWorkerIdPage(@Param("workerId") Long workerId,
                                                  @Param("offset") int offset,
                                                  @Param("pageSize") int pageSize);
    long countByWorkerId(@Param("workerId") Long workerId);
    BigDecimal sumMonthlyEarnings(@Param("workerId") Long workerId, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
    BigDecimal sumTotalEarnings(@Param("workerId") Long workerId);
    int deleteByRelatedWithdrawalId(@Param("relatedWithdrawalId") Long relatedWithdrawalId);
}
