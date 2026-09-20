package com.parttime.platform.mapper;

import com.parttime.platform.pojo.vo.CategorySupplyVO;
import com.parttime.platform.pojo.vo.EnterpriseActivityVO;
import com.parttime.platform.pojo.vo.EnterpriseTrendVO;
import com.parttime.platform.pojo.vo.WorkerActivityVO;
import com.parttime.platform.pojo.vo.WorkerTrendVO;
import org.apache.ibatis.annotations.Mapper;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface ReportMapper {

    Long countJobs();

    Long countApplications();

    Long countWorkers();

    Long countEnterprises();

    Long countTodayNewJobs();

    Long countTodayNewApplications();

    Long countTodayCompletedAttendance();

    BigDecimal sumPaidSettlement();

    BigDecimal sumTodayPaidSettlement();

    Long sumJobViews();

    Long countApprovedApplications();

    Long countCheckinRecords();

    Long countSettledRecords();

    List<EnterpriseTrendVO> enterpriseNewDaily();

    List<EnterpriseTrendVO> enterpriseJobDaily();

    List<WorkerTrendVO> workerNewDaily();

    List<WorkerTrendVO> workerAppDaily();

    List<EnterpriseActivityVO> enterpriseRanking();

    List<WorkerActivityVO> workerRanking();

    List<CategorySupplyVO> categorySupplyDemand();
}
