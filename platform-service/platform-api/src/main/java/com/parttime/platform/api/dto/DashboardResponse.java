package com.parttime.platform.api.dto;

import java.math.BigDecimal;
import java.util.List;

public class DashboardResponse {

    private int totalCompanies;
    private int activeJobs;
    private int totalWorkers;
    private int completedShifts;
    private BigDecimal totalTransactionAmount;
    private List<TrendDataPoint> recentTrend;

    public int getTotalCompanies() { return totalCompanies; }
    public void setTotalCompanies(int totalCompanies) { this.totalCompanies = totalCompanies; }

    public int getActiveJobs() { return activeJobs; }
    public void setActiveJobs(int activeJobs) { this.activeJobs = activeJobs; }

    public int getTotalWorkers() { return totalWorkers; }
    public void setTotalWorkers(int totalWorkers) { this.totalWorkers = totalWorkers; }

    public int getCompletedShifts() { return completedShifts; }
    public void setCompletedShifts(int completedShifts) { this.completedShifts = completedShifts; }

    public BigDecimal getTotalTransactionAmount() { return totalTransactionAmount; }
    public void setTotalTransactionAmount(BigDecimal totalTransactionAmount) { this.totalTransactionAmount = totalTransactionAmount; }

    public List<TrendDataPoint> getRecentTrend() { return recentTrend; }
    public void setRecentTrend(List<TrendDataPoint> recentTrend) { this.recentTrend = recentTrend; }

    public static class TrendDataPoint {
        private String date;
        private int count;

        public TrendDataPoint() {}

        public TrendDataPoint(String date, int count) {
            this.date = date;
            this.count = count;
        }

        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }

        public int getCount() { return count; }
        public void setCount(int count) { this.count = count; }
    }
}
