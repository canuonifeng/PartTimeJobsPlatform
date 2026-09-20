package com.parttime.platform.mapper;

import com.parttime.platform.pojo.entity.EnterpriseBalanceTransaction;
import com.parttime.platform.pojo.po.DailyAggRow;
import com.parttime.platform.pojo.vo.TransactionVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Mapper
public interface EnterpriseBalanceTransactionMapper {

    void insert(EnterpriseBalanceTransaction transaction);

    Optional<EnterpriseBalanceTransaction> findEntityById(@Param("id") Long id);

    List<TransactionVO> findByFilters(@Param("type") String type,
                                      @Param("startDate") String startDate,
                                      @Param("endDate") String endDate,
                                      @Param("companyId") Long companyId,
                                      @Param("keyword") String keyword);

    Optional<TransactionVO> findVoById(@Param("id") Long id);

    BigDecimal sumByTypeToday(@Param("type") String type);

    List<DailyAggRow> sumByTypeGroupByDate(@Param("type") String type,
                                           @Param("startDate") String startDate,
                                           @Param("endDate") String endDate);

    List<DailyAggRow> countByTypeGroupByDate(@Param("type") String type,
                                             @Param("startDate") String startDate,
                                             @Param("endDate") String endDate);
}
