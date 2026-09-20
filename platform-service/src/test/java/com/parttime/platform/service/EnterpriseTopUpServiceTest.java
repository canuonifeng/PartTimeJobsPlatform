package com.parttime.platform.service;

import com.parttime.platform.exception.BusinessException;
import com.parttime.platform.mapper.EnterpriseBalanceMapper;
import com.parttime.platform.mapper.EnterpriseBalanceTransactionMapper;
import com.parttime.platform.mapper.EnterpriseTopUpMapper;
import com.parttime.platform.pojo.entity.EnterpriseBalance;
import com.parttime.platform.pojo.entity.EnterpriseTopUp;
import com.parttime.platform.service.impl.EnterpriseTopUpServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnterpriseTopUpServiceTest {

    @Mock
    private EnterpriseTopUpMapper enterpriseTopUpMapper;

    @Mock
    private EnterpriseBalanceMapper enterpriseBalanceMapper;

    @Mock
    private EnterpriseBalanceTransactionMapper transactionMapper;

    @InjectMocks
    private EnterpriseTopUpServiceImpl topUpService;

    @Test
    void list_shouldReturnFiltered() {
        when(enterpriseTopUpMapper.findByFilters("PENDING", "kw")).thenReturn(List.of(new EnterpriseTopUp()));
        assertThat(topUpService.list("PENDING", "kw")).hasSize(1);
    }

    @Test
    void detail_shouldReturnRecord() {
        EnterpriseTopUp record = new EnterpriseTopUp();
        record.setId(1L);
        record.setAmount(new BigDecimal("5000.00"));
        when(enterpriseTopUpMapper.findById(1L)).thenReturn(Optional.of(record));

        EnterpriseTopUp result = topUpService.detail(1L);
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getAmount()).isEqualByComparingTo("5000.00");
    }

    @Test
    void detail_notFound_shouldThrow() {
        when(enterpriseTopUpMapper.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> topUpService.detail(99L)).isInstanceOf(BusinessException.class);
    }

    @Test
    void approve_shouldCreditBalanceAndInsertTransaction() {
        EnterpriseTopUp record = new EnterpriseTopUp();
        record.setId(1L);
        record.setCompanyId(10L);
        record.setAmount(new BigDecimal("100.00"));
        record.setStatus("PENDING");
        when(enterpriseTopUpMapper.findById(1L)).thenReturn(Optional.of(record));

        EnterpriseBalance eb = new EnterpriseBalance();
        eb.setBalance(new BigDecimal("0"));
        eb.setTotalTopUp(new BigDecimal("0"));
        eb.setTotalSpent(new BigDecimal("0"));
        when(enterpriseBalanceMapper.findByCompanyId(10L)).thenReturn(eb);

        topUpService.approve(1L, "admin", "ok");

        verify(enterpriseBalanceMapper).upsert(eq(10L), eq(new BigDecimal("100.00")), eq(new BigDecimal("100.00")), eq(new BigDecimal("0")));
        verify(transactionMapper).insert(any());
        verify(enterpriseTopUpMapper).updateStatus(1L, "APPROVED", "admin", "ok");
    }

    @Test
    void reject_shouldMarkRejected() {
        EnterpriseTopUp record = new EnterpriseTopUp();
        record.setId(1L);
        record.setStatus("PENDING");
        when(enterpriseTopUpMapper.findById(1L)).thenReturn(Optional.of(record));

        topUpService.reject(1L, "admin", "bad");

        verify(enterpriseTopUpMapper).updateStatus(1L, "REJECTED", "admin", "bad");
    }
}
