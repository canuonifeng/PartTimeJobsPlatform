package com.parttime.cservice.service;

import com.parttime.cservice.service.impl.WithdrawalServiceImpl;
import com.parttime.cservice.pojo.vo.EarningsSummaryVO;
import com.parttime.cservice.pojo.vo.WithdrawalVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WithdrawalServiceTest {

    @InjectMocks
    private WithdrawalServiceImpl withdrawalService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(withdrawalService, "withdrawalRecordMapper", InMemoryMappers.createWithdrawalRecordMapper());
    }

    @Test
    void requestWithdrawal_shouldCreatePendingRecord() {
        WithdrawalVO response = withdrawalService.requestWithdrawal(1L, new BigDecimal("500.00"));

        assertThat(response).isNotNull();
        assertThat(response.getWorkerId()).isEqualTo(1L);
        assertThat(response.getAmount()).isEqualByComparingTo(new BigDecimal("500.00"));
        assertThat(response.getStatus()).isEqualTo("PENDING");
        assertThat(response.getRequestedAt()).isNotNull();
    }

    @Test
    void requestWithdrawal_shouldThrowWhenAmountIsNull() {
        assertThatThrownBy(() -> withdrawalService.requestWithdrawal(1L, null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid");
    }

    @Test
    void requestWithdrawal_shouldThrowWhenAmountIsZero() {
        assertThatThrownBy(() -> withdrawalService.requestWithdrawal(1L, BigDecimal.ZERO))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid");
    }

    @Test
    void requestWithdrawal_shouldThrowWhenAmountIsNegative() {
        assertThatThrownBy(() -> withdrawalService.requestWithdrawal(1L, new BigDecimal("-100")))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid");
    }

    @Test
    void requestWithdrawal_shouldThrowWhenExceedsBalance() {
        withdrawalService.requestWithdrawal(1L, new BigDecimal("80000.00"));

        assertThatThrownBy(() -> withdrawalService.requestWithdrawal(1L, new BigDecimal("30000.00")))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Insufficient balance");
    }

    @Test
    void getWithdrawalHistory_shouldReturnWorkerRecords() {
        withdrawalService.requestWithdrawal(1L, new BigDecimal("100.00"));
        withdrawalService.requestWithdrawal(1L, new BigDecimal("200.00"));
        withdrawalService.requestWithdrawal(2L, new BigDecimal("300.00"));

        List<WithdrawalVO> history = withdrawalService.getWithdrawalHistory(1L);

        assertThat(history).hasSize(2);
    }

    @Test
    void getWithdrawalHistory_shouldReturnEmptyForNoRecords() {
        List<WithdrawalVO> history = withdrawalService.getWithdrawalHistory(999L);

        assertThat(history).isEmpty();
    }

    @Test
    void getEarningsSummary_shouldReturnZeroForNewWorker() {
        EarningsSummaryVO summary = withdrawalService.getEarningsSummary(999L);

        assertThat(summary).isNotNull();
        assertThat(summary.getTotalEarned()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(summary.getTotalWithdrawn()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(summary.getPendingWithdrawal()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void getEarningsSummary_shouldCalculateCorrectly() {
        withdrawalService.requestWithdrawal(1L, new BigDecimal("500.00"));
        withdrawalService.requestWithdrawal(1L, new BigDecimal("300.00"));

        EarningsSummaryVO summary = withdrawalService.getEarningsSummary(1L);

        assertThat(summary).isNotNull();
        assertThat(summary.getTotalEarned()).isEqualByComparingTo(new BigDecimal("800.00"));
        assertThat(summary.getPendingWithdrawal()).isEqualByComparingTo(new BigDecimal("800.00"));
        assertThat(summary.getTotalWithdrawn()).isEqualByComparingTo(BigDecimal.ZERO);
    }
}
