package com.parttime.platform.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
public class DashboardVO {

    @Schema(description = "企业总数")
    private int totalCompanies;
    @Schema(description = "活跃岗位数")
    private int activeJobs;
    @Schema(description = "工人总数")
    private int totalWorkers;
    @Schema(description = "已完成班次数")
    private int completedShifts;
    @Schema(description = "总交易金额")
    private BigDecimal totalTransactionAmount;
    @Schema(description = "近期趋势数据")
    private List<TrendDataPoint> recentTrend;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrendDataPoint {
        @Schema(description = "日期")
        private String date;
        @Schema(description = "数量")
        private int count;
    }
}
