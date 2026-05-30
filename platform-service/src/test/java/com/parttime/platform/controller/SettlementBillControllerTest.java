package com.parttime.platform.controller;

import com.parttime.platform.pojo.vo.PageVO;
import com.parttime.platform.pojo.vo.SettlementBillVO;
import com.parttime.platform.service.SettlementBillService;
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
class SettlementBillControllerTest {

    @Mock
    private SettlementBillService settlementBillService;

    @InjectMocks
    private SettlementBillController controller;

    @Test
    void listBills_shouldExposePlatformSettlementBills() {
        SettlementBillVO bill = new SettlementBillVO();
        bill.setId(1L);
        bill.setCompanyId(10L);
        bill.setWorkerName("张三");
        bill.setActualPay(new BigDecimal("120.00"));
        bill.setStatus("PAID");

        LocalDate dateFrom = LocalDate.of(2026, 5, 1);
        LocalDate dateTo = LocalDate.of(2026, 5, 31);
        when(settlementBillService.listBills(10L, "张", dateFrom, dateTo, "PAID", 2, 20))
                .thenReturn(new PageVO<>(List.of(bill), 1));

        PageVO<SettlementBillVO> result = controller.listBills(10L, "张", dateFrom, dateTo, "PAID", 2, 20);

        assertThat(result.getTotal()).isEqualTo(1L);
        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getRecords().get(0).getWorkerName()).isEqualTo("张三");
        verify(settlementBillService).listBills(10L, "张", dateFrom, dateTo, "PAID", 2, 20);
    }
}
