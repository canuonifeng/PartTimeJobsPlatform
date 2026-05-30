package com.parttime.platform.service;

import com.parttime.platform.mapper.SettlementBillMapper;
import com.parttime.platform.pojo.entity.SettlementBill;
import com.parttime.platform.pojo.vo.PageVO;
import com.parttime.platform.pojo.vo.SettlementBillVO;
import com.parttime.platform.service.impl.SettlementBillServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SettlementBillServiceTest {

    @Mock
    private SettlementBillMapper settlementBillMapper;

    @InjectMocks
    private SettlementBillServiceImpl settlementBillService;

    @Test
    void listBills_shouldReturnPagedSettlementBillsForPlatform() {
        SettlementBill bill = new SettlementBill();
        bill.setId(1L);
        bill.setCompanyId(10L);
        bill.setCompanyName("测试企业");
        bill.setWorkerName("张三");
        bill.setActualPay(new BigDecimal("120.00"));
        bill.setStatus("PAID");

        when(settlementBillMapper.findPage(10L, "张", LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 31), "PAID", 20, 20))
                .thenReturn(List.of(bill));
        when(settlementBillMapper.countPage(10L, "张", LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 31), "PAID"))
                .thenReturn(1L);

        PageVO<SettlementBillVO> result = settlementBillService.listBills(10L, "张", LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 31), "PAID", 2, 20);

        assertThat(result.getTotal()).isEqualTo(1L);
        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getRecords().get(0).getId()).isEqualTo(1L);
        assertThat(result.getRecords().get(0).getCompanyName()).isEqualTo("测试企业");
        assertThat(result.getRecords().get(0).getActualPay()).isEqualByComparingTo("120.00");
        verify(settlementBillMapper).findPage(10L, "张", LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 31), "PAID", 20, 20);
    }
}
