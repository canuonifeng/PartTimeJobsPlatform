package com.parttime.platform.mapper;

import com.parttime.platform.pojo.vo.DashboardVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DashboardTrendMapper {

    List<DashboardVO.TrendDataPoint> countNewJobs(@Param("start") String start);

    List<DashboardVO.TrendDataPoint> countNewWorkers(@Param("start") String start);

    List<DashboardVO.TrendDataPoint> countCompletedShifts(@Param("start") String start);

    int countTotalCompanies();

    int countActiveJobs();

    int countTotalWorkers();

    int countCompletedShiftsTotal();

    java.math.BigDecimal sumTransactionAmount();
}
