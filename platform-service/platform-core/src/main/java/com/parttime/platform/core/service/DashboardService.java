package com.parttime.platform.core.service;

import com.parttime.platform.api.dto.DashboardResponse;
import com.parttime.platform.api.dto.DashboardResponse.TrendDataPoint;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

public class DashboardService {

    public DashboardResponse getDashboardStats() {
        DashboardResponse response = new DashboardResponse();
        response.setTotalCompanies(150);
        response.setActiveJobs(42);
        response.setTotalWorkers(1280);
        response.setCompletedShifts(5600);
        response.setTotalTransactionAmount(new BigDecimal("125000.00"));
        response.setRecentTrend(getMockTrend());
        return response;
    }

    private List<TrendDataPoint> getMockTrend() {
        return Arrays.asList(
                new TrendDataPoint("2026-05-09", 120),
                new TrendDataPoint("2026-05-10", 135),
                new TrendDataPoint("2026-05-11", 110),
                new TrendDataPoint("2026-05-12", 150),
                new TrendDataPoint("2026-05-13", 142),
                new TrendDataPoint("2026-05-14", 168),
                new TrendDataPoint("2026-05-15", 155)
        );
    }
}
