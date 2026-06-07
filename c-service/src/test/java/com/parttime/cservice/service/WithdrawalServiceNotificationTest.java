package com.parttime.cservice.service;

import com.parttime.cservice.mapper.BalanceTransactionMapper;
import com.parttime.cservice.mapper.WithdrawalRecordMapper;
import com.parttime.cservice.mapper.WorkerBalanceMapper;
import com.parttime.cservice.mapper.WorkerBankCardMapper;
import com.parttime.cservice.mapper.WorkerRealNameAuthMapper;
import com.parttime.cservice.pojo.entity.WorkerBalance;
import com.parttime.cservice.pojo.entity.WorkerBankCard;
import com.parttime.cservice.pojo.entity.WorkerRealNameAuth;
import com.parttime.cservice.service.impl.WithdrawalServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WithdrawalServiceNotificationTest {

    @Mock
    private WithdrawalRecordMapper withdrawalRecordMapper;
    @Mock
    private WorkerBalanceMapper workerBalanceMapper;
    @Mock
    private BalanceTransactionMapper balanceTransactionMapper;
    @Mock
    private WorkerRealNameAuthMapper workerRealNameAuthMapper;
    @Mock
    private WorkerBankCardMapper workerBankCardMapper;
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private WithdrawalServiceImpl withdrawalService;

    @Test
    void requestWithdrawal_shouldNotifyWorkerWhenCompleted() {
        WorkerRealNameAuth auth = new WorkerRealNameAuth();
        auth.setStatus("APPROVED");
        WorkerBankCard card = new WorkerBankCard();
        card.setBankName("ICBC");
        card.setCardHolder("张三");
        card.setCardNumber("6222021234567890");
        WorkerBalance balance = new WorkerBalance();
        balance.setBalance(new BigDecimal("200.00"));
        balance.setTotalEarned(new BigDecimal("500.00"));
        balance.setTotalWithdrawn(new BigDecimal("100.00"));

        when(workerRealNameAuthMapper.findByWorkerId(1L)).thenReturn(Optional.of(auth));
        when(workerBankCardMapper.findByWorkerId(1L)).thenReturn(Optional.of(card));
        when(workerBalanceMapper.findByWorkerId(1L)).thenReturn(balance);

        withdrawalService.requestWithdrawal(1L, new BigDecimal("80.00"));

        verify(withdrawalRecordMapper).insert(any());
        verify(notificationService).createWorkerNotification(
                1L,
                "WITHDRAWAL_COMPLETED",
                "finance",
                "提现成功",
                "您申请的提现80.00元已处理完成",
                "WITHDRAWAL",
                null);
    }
}
