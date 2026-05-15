package com.parttime.platform.service.impl;

import com.parttime.platform.pojo.vo.DashboardVO;
import com.parttime.platform.service.DashboardService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Service
public class DashboardServiceImpl implements DashboardService {

    @Override
    public DashboardVO getDashboardStats() {
        DashboardVO vo = new DashboardVO();
        vo.setTotalCompanies(150);
        vo.setActiveJobs(42);
        vo.setTotalWorkers(1280);
        vo.setCompletedShifts(5600);
        vo.setTotalTransactionAmount(new BigDecimal("125000.00"));
        vo.setRecentTrend(getMockTrend());
        return vo;
    }

    private List<DashboardVO.TrendDataPoint> getMockTrend() {
        return Arrays.asList(
                new DashboardVO.TrendDataPoint("2026-05-09", 120),
                new DashboardVO.TrendDataPoint("2026-05-10", 135),
                new DashboardVO.TrendDataPoint("2026-05-11", 110),
                new DashboardVO.TrendDataPoint("2026-05-12", 150),
                new DashboardVO.TrendDataPoint("2026-05-13", 142),
                new DashboardVO.TrendDataPoint("2026-05-14", 168),
                new DashboardVO.TrendDataPoint("2026-05-15", 155)
        );
    }
}
