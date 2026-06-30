package com.parttime.platform.service;

import com.parttime.platform.pojo.entity.EnterpriseTopUp;
import com.parttime.platform.service.impl.EnterpriseTopUpServiceImpl;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EnterpriseTopUpServiceTest {

    private final EnterpriseTopUpServiceImpl topUpService = new EnterpriseTopUpServiceImpl();

    @Test
    void list_shouldReturnList() {
        List<EnterpriseTopUp> result = topUpService.list("PENDING", "Test");
        assertThat(result).isNotEmpty();
    }

    @Test
    void detail_shouldReturnRecord() {
        EnterpriseTopUp result = topUpService.detail(1L);
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("5000.00"));
    }

    @Test
    void approve_shouldNotThrow() {
        topUpService.approve(1L, "admin", "approved");
    }

    @Test
    void reject_shouldNotThrow() {
        topUpService.reject(1L, "admin", "invalid");
    }
}
