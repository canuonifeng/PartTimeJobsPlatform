package com.parttime.cservice.service;

import com.parttime.cservice.mapper.BalanceTransactionMapper;
import com.parttime.cservice.mapper.WorkerBalanceMapper;
import com.parttime.cservice.mapper.WorkerBankCardMapper;
import com.parttime.cservice.mapper.WorkerRealNameAuthMapper;
import com.parttime.cservice.service.impl.WithdrawalServiceImpl;
import com.parttime.cservice.pojo.entity.WorkerBankCard;
import com.parttime.cservice.pojo.entity.WorkerRealNameAuth;
import com.parttime.cservice.pojo.vo.WithdrawalVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import com.parttime.cservice.pojo.vo.PageVO;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WithdrawalServiceTest {

    @InjectMocks
    private WithdrawalServiceImpl withdrawalService;

    private WorkerRealNameAuthMapper realNameAuthMapper;
    private WorkerBankCardMapper bankCardMapper;
    private WorkerBalanceMapper workerBalanceMapper;
    private BalanceTransactionMapper balanceTransactionMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        realNameAuthMapper = InMemoryMappers.createWorkerRealNameAuthMapper();
        bankCardMapper = InMemoryMappers.createWorkerBankCardMapper();
        workerBalanceMapper = InMemoryMappers.createWorkerBalanceMapper();
        balanceTransactionMapper = InMemoryMappers.createBalanceTransactionMapper();
        ReflectionTestUtils.setField(withdrawalService, "withdrawalRecordMapper", InMemoryMappers.createWithdrawalRecordMapper());
        ReflectionTestUtils.setField(withdrawalService, "workerRealNameAuthMapper", realNameAuthMapper);
        ReflectionTestUtils.setField(withdrawalService, "workerBankCardMapper", bankCardMapper);
        ReflectionTestUtils.setField(withdrawalService, "workerBalanceMapper", workerBalanceMapper);
        ReflectionTestUtils.setField(withdrawalService, "balanceTransactionMapper", balanceTransactionMapper);
        ReflectionTestUtils.setField(withdrawalService, "notificationService", InMemoryMappers.createNotificationService());
        ReflectionTestUtils.setField(withdrawalService, "weChatPayService", InMemoryMappers.createWeChatPayService());
    }

    private void setupApprovedRealName(Long workerId) {
        WorkerRealNameAuth auth = new WorkerRealNameAuth();
        auth.setWorkerId(workerId);
        auth.setStatus("APPROVED");
        realNameAuthMapper.insert(auth);
    }

    private void setupBankCard(Long workerId) {
        WorkerBankCard card = new WorkerBankCard();
        card.setWorkerId(workerId);
        card.setCardHolder("Zhang");
        card.setCardNumber("6222021234567890");
        card.setBankName("ICBC");
        bankCardMapper.insert(card);
    }

    @Test
    void requestWithdrawal_shouldThrowWhenRealNameNotApproved() {
        assertThatThrownBy(() -> withdrawalService.requestWithdrawal(1L, new BigDecimal("100"), "WECHAT", null))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void requestWithdrawal_shouldThrowWhenRealNamePending() {
        WorkerRealNameAuth auth = new WorkerRealNameAuth();
        auth.setWorkerId(1L);
        auth.setStatus("PENDING");
        realNameAuthMapper.insert(auth);
        assertThatThrownBy(() -> withdrawalService.requestWithdrawal(1L, new BigDecimal("100"), "WECHAT", null))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void requestWithdrawal_shouldThrowWhenNoBankCard() {
        setupApprovedRealName(1L);
        assertThatThrownBy(() -> withdrawalService.requestWithdrawal(1L, new BigDecimal("100"), "BANK_CARD", null))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void requestWithdrawal_shouldThrowWhenAmountIsNull() {
        assertThatThrownBy(() -> withdrawalService.requestWithdrawal(1L, null, "WECHAT", null))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void requestWithdrawal_shouldThrowWhenAmountIsZero() {
        assertThatThrownBy(() -> withdrawalService.requestWithdrawal(1L, BigDecimal.ZERO, "WECHAT", null))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void requestWithdrawal_shouldThrowWhenAmountIsBelowMinimum() {
        assertThatThrownBy(() -> withdrawalService.requestWithdrawal(1L, new BigDecimal("0.5"), "WECHAT", null))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void requestWithdrawal_shouldThrowWhenAmountExceedsMaximum() {
        assertThatThrownBy(() -> withdrawalService.requestWithdrawal(1L, new BigDecimal("1001"), "WECHAT", null))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void requestWithdrawal_shouldThrowWhenUnsupportedMethod() {
        setupApprovedRealName(1L);
        assertThatThrownBy(() -> withdrawalService.requestWithdrawal(1L, new BigDecimal("100"), "UNSUPPORTED", null))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void getWithdrawalHistory_shouldReturnEmptyForNoRecords() {
        PageVO<WithdrawalVO> page = withdrawalService.getWithdrawalHistory(999L, 1, 20);
        assertThat(page.getRecords()).isEmpty();
        assertThat(page.getTotal()).isZero();
    }
}
