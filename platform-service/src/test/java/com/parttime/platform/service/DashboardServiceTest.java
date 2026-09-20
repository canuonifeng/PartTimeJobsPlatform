package com.parttime.platform.service;

import com.parttime.platform.mapper.DashboardTrendMapper;
import com.parttime.platform.pojo.vo.DashboardVO;
import com.parttime.platform.service.impl.DashboardServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private DashboardTrendMapper dashboardTrendMapper;

    @InjectMocks
    private DashboardServiceImpl dashboardService;

    @Test
    void getDashboardStats_shouldAggregateFromMapper() {
        when(dashboardTrendMapper.countTotalCompanies()).thenReturn(15);
        when(dashboardTrendMapper.countActiveJobs()).thenReturn(8);
        when(dashboardTrendMapper.countTotalWorkers()).thenReturn(320);
        when(dashboardTrendMapper.countCompletedShiftsTotal()).thenReturn(90);
        when(dashboardTrendMapper.sumTransactionAmount()).thenReturn(new BigDecimal("888.00"));
        when(dashboardTrendMapper.countCompletedShifts(org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(List.of(new DashboardVO.TrendDataPoint("2026-09-20", 3)));

        DashboardVO response = dashboardService.getDashboardStats();

        assertThat(response.getTotalCompanies()).isEqualTo(15);
        assertThat(response.getActiveJobs()).isEqualTo(8);
        assertThat(response.getTotalWorkers()).isEqualTo(320);
        assertThat(response.getCompletedShifts()).isEqualTo(90);
        assertThat(response.getTotalTransactionAmount()).isEqualByComparingTo("888.00");
        assertThat(response.getRecentTrend()).hasSize(1);
    }
}
