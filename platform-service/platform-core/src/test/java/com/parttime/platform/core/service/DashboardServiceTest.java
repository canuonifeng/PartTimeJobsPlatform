package com.parttime.platform.core.service;

import com.parttime.platform.api.dto.DashboardResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    private final DashboardService dashboardService = new DashboardService();

    @Test
    void getDashboardStats_shouldReturnMockData() {
        DashboardResponse response = dashboardService.getDashboardStats();

        assertThat(response.getTotalCompanies()).isEqualTo(150);
        assertThat(response.getActiveJobs()).isEqualTo(42);
        assertThat(response.getTotalWorkers()).isEqualTo(1280);
        assertThat(response.getCompletedShifts()).isEqualTo(5600);
        assertThat(response.getTotalTransactionAmount()).isEqualByComparingTo(new BigDecimal("125000.00"));
        assertThat(response.getRecentTrend()).hasSize(7);
    }
}
