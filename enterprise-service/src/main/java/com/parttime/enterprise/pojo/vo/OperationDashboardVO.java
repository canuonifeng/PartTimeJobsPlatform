package com.parttime.enterprise.pojo.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class OperationDashboardVO {
    private OperationOverviewVO overview;
    private OperationTodayVO today;
    private List<OperationProcessNodeVO> process;
    private List<OperationTodoSummaryVO> todoSummary;
    private TrendSummary trendSummary;

    @Data
    public static class TrendSummary {
        private Long applicationTotal;
        private Long shiftTotal;
        private BigDecimal salaryTotalAmount;
    }
}
