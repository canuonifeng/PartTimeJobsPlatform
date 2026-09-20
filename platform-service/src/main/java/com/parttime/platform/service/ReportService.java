package com.parttime.platform.service;

import com.parttime.platform.pojo.vo.ConversionFunnelVO;
import com.parttime.platform.pojo.vo.EnterpriseActivityVO;
import com.parttime.platform.pojo.vo.EnterpriseTrendVO;
import com.parttime.platform.pojo.vo.OverviewVO;
import com.parttime.platform.pojo.vo.SupplyDemandVO;
import com.parttime.platform.pojo.vo.WorkerActivityVO;
import com.parttime.platform.pojo.vo.WorkerTrendVO;

import java.util.List;

public interface ReportService {

    OverviewVO overview();

    List<EnterpriseTrendVO> enterpriseActivity();

    List<WorkerTrendVO> workerActivity();

    List<EnterpriseActivityVO> enterpriseRanking();

    List<WorkerActivityVO> workerRanking();

    SupplyDemandVO supplyDemandAnalysis();

    ConversionFunnelVO conversionFunnel();
}
