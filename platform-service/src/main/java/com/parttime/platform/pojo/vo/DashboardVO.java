package com.parttime.platform.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class DashboardVO {

    private int totalCompanies;
    private int activeJobs;
    private int totalWorkers;
    private int completedShifts;
    private BigDecimal totalTransactionAmount;
    private List<TrendDataPoint> recentTrend;

    @Data
    @AllArgsConstructor
    public static class TrendDataPoint {
        private String date;
        private int count;
    }
}
