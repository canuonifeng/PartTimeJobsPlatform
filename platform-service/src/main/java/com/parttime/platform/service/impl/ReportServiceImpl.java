package com.parttime.platform.service.impl;

import com.parttime.platform.mapper.ReportMapper;
import com.parttime.platform.mapper.SystemConfigMapper;
import com.parttime.platform.pojo.entity.SystemConfig;
import com.parttime.platform.pojo.vo.CategorySupplyVO;
import com.parttime.platform.pojo.vo.ConversionFunnelVO;
import com.parttime.platform.pojo.vo.EnterpriseActivityVO;
import com.parttime.platform.pojo.vo.EnterpriseTrendVO;
import com.parttime.platform.pojo.vo.FunnelStepVO;
import com.parttime.platform.pojo.vo.OverviewVO;
import com.parttime.platform.pojo.vo.SupplyDemandVO;
import com.parttime.platform.pojo.vo.WorkerActivityVO;
import com.parttime.platform.pojo.vo.WorkerTrendVO;
import com.parttime.platform.service.ReportService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportServiceImpl implements ReportService {

    @Resource
    private ReportMapper reportMapper;

    @Resource
    private SystemConfigMapper systemConfigMapper;

    @Override
    public OverviewVO overview() {
        OverviewVO vo = new OverviewVO();
        vo.setTotalJobs(safe(reportMapper.countJobs()));
        vo.setTotalApplications(safe(reportMapper.countApplications()));
        vo.setTotalWorkers(safe(reportMapper.countWorkers()));
        vo.setTotalEnterprises(safe(reportMapper.countEnterprises()));
        vo.setTodayNewJobs(safe(reportMapper.countTodayNewJobs()));
        vo.setTodayNewApplications(safe(reportMapper.countTodayNewApplications()));
        vo.setTodayCompletedAttendance(safe(reportMapper.countTodayCompletedAttendance()));
        vo.setTodaySettlementAmount(nz(reportMapper.sumTodayPaidSettlement()));
        BigDecimal totalSettlement = nz(reportMapper.sumPaidSettlement());
        vo.setTotalSettlementAmount(totalSettlement);
        vo.setTotalServiceFee(totalSettlement.multiply(feeRate()).setScale(2, RoundingMode.HALF_UP));
        return vo;
    }

    @Override
    public List<EnterpriseTrendVO> enterpriseActivity() {
        Map<String, EnterpriseTrendVO> map = new LinkedHashMap<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate d = LocalDate.now().minusDays(i);
            EnterpriseTrendVO vo = new EnterpriseTrendVO();
            vo.setDate(d.toString());
            vo.setNewEnterprises(0L);
            vo.setActiveEnterprises(0L);
            vo.setPublishedJobs(0L);
            map.put(d.toString(), vo);
        }
        for (EnterpriseTrendVO item : reportMapper.enterpriseNewDaily()) {
            EnterpriseTrendVO vo = map.get(item.getDate());
            if (vo != null && item.getNewEnterprises() != null) {
                vo.setNewEnterprises(item.getNewEnterprises());
            }
        }
        for (EnterpriseTrendVO item : reportMapper.enterpriseJobDaily()) {
            EnterpriseTrendVO vo = map.get(item.getDate());
            if (vo != null) {
                if (item.getPublishedJobs() != null) {
                    vo.setPublishedJobs(item.getPublishedJobs());
                }
                if (item.getActiveEnterprises() != null) {
                    vo.setActiveEnterprises(item.getActiveEnterprises());
                }
            }
        }
        return new ArrayList<>(map.values());
    }

    @Override
    public List<WorkerTrendVO> workerActivity() {
        Map<String, WorkerTrendVO> map = new LinkedHashMap<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate d = LocalDate.now().minusDays(i);
            WorkerTrendVO vo = new WorkerTrendVO();
            vo.setDate(d.toString());
            vo.setNewWorkers(0L);
            vo.setActiveWorkers(0L);
            vo.setApplications(0L);
            map.put(d.toString(), vo);
        }
        for (WorkerTrendVO item : reportMapper.workerNewDaily()) {
            WorkerTrendVO vo = map.get(item.getDate());
            if (vo != null && item.getNewWorkers() != null) {
                vo.setNewWorkers(item.getNewWorkers());
            }
        }
        for (WorkerTrendVO item : reportMapper.workerAppDaily()) {
            WorkerTrendVO vo = map.get(item.getDate());
            if (vo != null) {
                if (item.getApplications() != null) {
                    vo.setApplications(item.getApplications());
                }
                if (item.getActiveWorkers() != null) {
                    vo.setActiveWorkers(item.getActiveWorkers());
                }
            }
        }
        return new ArrayList<>(map.values());
    }

    @Override
    public List<EnterpriseActivityVO> enterpriseRanking() {
        List<EnterpriseActivityVO> list = reportMapper.enterpriseRanking();
        long maxApp = list.isEmpty() ? 1 : Math.max(1, list.get(0).getApplicationCount() == null ? 0 : list.get(0).getApplicationCount());
        for (int i = 0; i < list.size(); i++) {
            EnterpriseActivityVO vo = list.get(i);
            vo.setRank(i + 1);
            long app = vo.getApplicationCount() == null ? 0 : vo.getApplicationCount();
            double ratio = (double) app / maxApp;
            vo.setActivityLevel(ratio >= 0.6 ? "HIGH" : ratio >= 0.3 ? "MEDIUM" : "LOW");
        }
        return list;
    }

    @Override
    public List<WorkerActivityVO> workerRanking() {
        List<WorkerActivityVO> list = reportMapper.workerRanking();
        BigDecimal maxEarning = list.isEmpty() ? BigDecimal.ONE :
                list.get(0).getTotalEarnings() == null ? BigDecimal.ONE : list.get(0).getTotalEarnings();
        if (maxEarning.compareTo(BigDecimal.ZERO) <= 0) {
            maxEarning = BigDecimal.ONE;
        }
        for (int i = 0; i < list.size(); i++) {
            WorkerActivityVO vo = list.get(i);
            vo.setRank(i + 1);
            BigDecimal earning = vo.getTotalEarnings() == null ? BigDecimal.ZERO : vo.getTotalEarnings();
            double ratio = earning.divide(maxEarning, 2, RoundingMode.HALF_UP).doubleValue();
            vo.setActivityLevel(ratio >= 0.6 ? "HIGH" : ratio >= 0.3 ? "MEDIUM" : "LOW");
        }
        return list;
    }

    @Override
    public SupplyDemandVO supplyDemandAnalysis() {
        List<CategorySupplyVO> categories = reportMapper.categorySupplyDemand();
        SupplyDemandVO vo = new SupplyDemandVO();
        long totalDemand = 0;
        long totalSupply = 0;
        for (CategorySupplyVO c : categories) {
            totalDemand += c.getDemand() == null ? 0 : c.getDemand();
            totalSupply += c.getSupply() == null ? 0 : c.getSupply();
        }
        vo.setCategories(categories);
        vo.setTotalDemand(totalDemand);
        vo.setTotalSupply(totalSupply);
        if (totalDemand == 0) {
            vo.setMatchingRate(BigDecimal.ZERO);
        } else {
            vo.setMatchingRate(BigDecimal.valueOf(totalSupply)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(totalDemand), 1, RoundingMode.HALF_UP));
        }
        return vo;
    }

    @Override
    public ConversionFunnelVO conversionFunnel() {
        ConversionFunnelVO vo = new ConversionFunnelVO();
        Long browse = safe(reportMapper.sumJobViews());
        Long apply = safe(reportMapper.countApplications());
        Long approved = safe(reportMapper.countApprovedApplications());
        Long checkin = safe(reportMapper.countCheckinRecords());
        Long settled = safe(reportMapper.countSettledRecords());
        vo.setBrowseCount(browse);
        vo.setViewCount(apply);
        vo.setApplyCount(apply);
        vo.setApprovedCount(approved);
        vo.setCheckinCount(checkin);
        vo.setSettlementCount(settled);

        List<FunnelStepVO> steps = new ArrayList<>();
        steps.add(buildStep("浏览职位", browse));
        steps.add(buildStep("查看详情", apply));
        steps.add(buildStep("报名申请", apply));
        steps.add(buildStep("审核通过", approved));
        steps.add(buildStep("实际到岗", checkin));
        steps.add(buildStep("完成结算", settled));
        vo.setSteps(steps);
        return vo;
    }

    private FunnelStepVO buildStep(String name, Long count) {
        FunnelStepVO step = new FunnelStepVO();
        step.setName(name);
        step.setCount(count == null ? 0L : count);
        return step;
    }

    private BigDecimal feeRate() {
        return systemConfigMapper.findByKey("platform_fee_rate")
                .map(SystemConfig::getConfigValue)
                .map(v -> new BigDecimal(v))
                .orElse(new BigDecimal("0.10"));
    }

    private Long safe(Long v) {
        return v == null ? 0L : v;
    }

    private BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}
