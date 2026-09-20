package com.parttime.platform.mapper;

import com.parttime.platform.pojo.po.DailyAggRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface FinanceReportMapper {

    List<DailyAggRow> sumApprovedTopUpGroupByDate(@Param("startDate") String startDate,
                                                  @Param("endDate") String endDate);

    BigDecimal sumApprovedTopUpBetween(@Param("startDate") String startDate,
                                       @Param("endDate") String endDate);

    List<DailyAggRow> sumCompletedWithdrawalGroupByDate(@Param("startDate") String startDate,
                                                         @Param("endDate") String endDate);

    BigDecimal sumCompletedWithdrawalBetween(@Param("startDate") String startDate,
                                            @Param("endDate") String endDate);

    BigDecimal sumCompletedWithdrawalToday();
}
