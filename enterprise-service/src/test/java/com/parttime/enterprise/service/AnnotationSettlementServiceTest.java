package com.parttime.enterprise.service;

import com.parttime.enterprise.mapper.BalanceTransactionMapper;
import com.parttime.enterprise.mapper.WorkerBalanceMapper;
import com.parttime.enterprise.mapper.WorkerNotificationMapper;
import com.parttime.enterprise.pojo.entity.AnnotationTaskOrder;
import com.parttime.enterprise.pojo.entity.BalanceTransaction;
import com.parttime.enterprise.pojo.entity.Job;
import com.parttime.enterprise.pojo.entity.WorkerBalance;
import com.parttime.enterprise.service.impl.AnnotationSettlementServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnnotationSettlementServiceTest {

    @Mock
    private WorkerBalanceMapper workerBalanceMapper;
    @Mock
    private BalanceTransactionMapper balanceTransactionMapper;
    @Mock
    private WorkerNotificationMapper workerNotificationMapper;

    @InjectMocks
    private AnnotationSettlementServiceImpl settlementService;

    private Job perItemJob() {
        Job job = new Job();
        job.setId(1L);
        job.setTitle("标注任务包");
        job.setPricingMode("PER_ITEM");
        job.setPricePerUnit(new BigDecimal("0.50"));
        return job;
    }

    private AnnotationTaskOrder order() {
        AnnotationTaskOrder o = new AnnotationTaskOrder();
        o.setId(1L);
        o.setWorkerId(100L);
        o.setJobId(1L);
        o.setCompletedItems(80);
        return o;
    }

    @Test
    void settle_perItem_shouldPayItemsTimesUnitPrice() {
        when(workerBalanceMapper.findByWorkerId(100L)).thenReturn(null);

        settlementService.settleAnnotationTask(order(), perItemJob());

        verify(workerBalanceMapper).upsert(100L, new BigDecimal("40.00"), new BigDecimal("40.00"), BigDecimal.ZERO);
        ArgumentCaptor<BalanceTransaction> captor = ArgumentCaptor.forClass(BalanceTransaction.class);
        verify(balanceTransactionMapper).insert(captor.capture());
        assertThat(captor.getValue().getAmount()).isEqualByComparingTo(new BigDecimal("40.00"));
        assertThat(captor.getValue().getType()).isEqualTo("EARNINGS");
        assertThat(captor.getValue().getWorkerId()).isEqualTo(100L);
    }

    @Test
    void settle_perPackage_shouldPayPackagePrice() {
        Job job = perItemJob();
        job.setPricingMode("PER_PACKAGE");
        job.setPricePerUnit(new BigDecimal("300.00"));
        when(workerBalanceMapper.findByWorkerId(100L)).thenReturn(null);

        settlementService.settleAnnotationTask(order(), job);

        verify(workerBalanceMapper).upsert(100L, new BigDecimal("300.00"), new BigDecimal("300.00"), BigDecimal.ZERO);
    }

    @Test
    void settle_existingBalance_shouldAccumulate() {
        WorkerBalance wb = new WorkerBalance();
        wb.setWorkerId(100L);
        wb.setBalance(new BigDecimal("100.00"));
        wb.setTotalEarned(new BigDecimal("1000.00"));
        wb.setTotalWithdrawn(new BigDecimal("900.00"));
        when(workerBalanceMapper.findByWorkerId(100L)).thenReturn(wb);

        settlementService.settleAnnotationTask(order(), perItemJob());

        verify(workerBalanceMapper).upsert(100L, new BigDecimal("140.00"), new BigDecimal("1040.00"), new BigDecimal("900.00"));
    }

    @Test
    void settle_missingUnitPrice_shouldReject() {
        Job job = perItemJob();
        job.setPricePerUnit(null);

        assertThatThrownBy(() -> settlementService.settleAnnotationTask(order(), job))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("缺少单价");
    }

    @Test
    void settle_shouldMarkSettledAndNotify() {
        when(workerBalanceMapper.findByWorkerId(100L)).thenReturn(null);

        AnnotationTaskOrder taskOrder = order();
        settlementService.settleAnnotationTask(taskOrder, perItemJob());

        assertThat(taskOrder.getSettledAt()).isNotNull();
        assertThat(taskOrder.getStatus()).isEqualTo("COMPLETED");
        verify(workerNotificationMapper).insertWorkerNotification(eq(100L), any(), any(), any(), any(), any(), any());
    }
}
