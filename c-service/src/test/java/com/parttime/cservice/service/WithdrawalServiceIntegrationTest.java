package com.parttime.cservice.service;

import com.parttime.cservice.mapper.BalanceTransactionMapper;
import com.parttime.cservice.mapper.WorkerBalanceMapper;
import com.parttime.cservice.mapper.WorkerBankCardMapper;
import com.parttime.cservice.mapper.WorkerRealNameAuthMapper;
import com.parttime.cservice.mapper.WithdrawalRecordMapper;
import com.parttime.cservice.pojo.entity.BalanceTransaction;
import com.parttime.cservice.pojo.entity.WorkerBankCard;
import com.parttime.cservice.pojo.entity.WorkerRealNameAuth;
import com.parttime.cservice.pojo.vo.WithdrawalVO;
import com.parttime.cservice.service.impl.WithdrawalServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WithdrawalServiceIntegrationTest {

    @InjectMocks
    private WithdrawalServiceImpl withdrawalService;

    private WorkerRealNameAuthMapper realNameAuthMapper;
    private WorkerBankCardMapper bankCardMapper;
    private WorkerBalanceMapper workerBalanceMapper;
    private BalanceTransactionMapper balanceTransactionMapper;
    private WithdrawalRecordMapper withdrawalRecordMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        realNameAuthMapper = InMemoryMappers.createWorkerRealNameAuthMapper();
        bankCardMapper = InMemoryMappers.createWorkerBankCardMapper();
        workerBalanceMapper = InMemoryMappers.createWorkerBalanceMapper();
        balanceTransactionMapper = InMemoryMappers.createBalanceTransactionMapper();
        withdrawalRecordMapper = InMemoryMappers.createWithdrawalRecordMapper();
        ReflectionTestUtils.setField(withdrawalService, "withdrawalRecordMapper", withdrawalRecordMapper);
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

    private void setupBalance(Long workerId, BigDecimal balance) {
        workerBalanceMapper.upsert(workerId, balance, new BigDecimal("10000"), BigDecimal.ZERO);
    }

    @Test
    void requestWithdrawal_wechat_shouldSucceed() {
        Long workerId = 100L;
        setupApprovedRealName(workerId);
        setupBalance(workerId, BigDecimal.valueOf(500));

        WithdrawalVO result = withdrawalService.requestWithdrawal(
                workerId,
                BigDecimal.valueOf(100),
                "WECHAT",
                null
        );

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getWorkerId()).isEqualTo(workerId);
        assertThat(result.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(100));
        assertThat(result.getStatus()).isEqualTo("COMPLETED");
        assertThat(result.getThirdPartySerialNo()).isNotNull();
        assertThat(result.getThirdPartyPlatform()).isEqualTo("WECHAT_PAY");
    }

    @Test
    void requestWithdrawal_bankCard_shouldSucceed() {
        Long workerId = 101L;
        setupApprovedRealName(workerId);
        setupBankCard(workerId);
        setupBalance(workerId, BigDecimal.valueOf(500));

        WithdrawalVO result = withdrawalService.requestWithdrawal(
                workerId,
                BigDecimal.valueOf(200),
                "BANK_CARD",
                1L
        );

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getWorkerId()).isEqualTo(workerId);
        assertThat(result.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(200));
        assertThat(result.getStatus()).isEqualTo("COMPLETED");
        assertThat(result.getThirdPartySerialNo()).isNotNull();
        assertThat(result.getThirdPartyPlatform()).isEqualTo("WECHAT_PAY");
    }

    @Test
    void requestWithdrawal_shouldDeductBalance() {
        Long workerId = 102L;
        setupApprovedRealName(workerId);
        setupBalance(workerId, BigDecimal.valueOf(500));

        withdrawalService.requestWithdrawal(workerId, BigDecimal.valueOf(100), "WECHAT", null);

        var balance = workerBalanceMapper.findByWorkerId(workerId);
        assertThat(balance.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(400));
        assertThat(balance.getTotalWithdrawn()).isEqualByComparingTo(BigDecimal.valueOf(100));
    }

    @Test
    void requestWithdrawal_shouldCreateTransactionRecord() {
        Long workerId = 103L;
        setupApprovedRealName(workerId);
        setupBalance(workerId, BigDecimal.valueOf(500));

        WithdrawalVO result = withdrawalService.requestWithdrawal(workerId, BigDecimal.valueOf(100), "WECHAT", null);

        List<BalanceTransaction> transactions = balanceTransactionMapper.findByWorkerIdPage(workerId, 0, 100);
        assertThat(transactions).hasSize(1);
        BalanceTransaction bt = transactions.get(0);
        assertThat(bt.getType()).isEqualTo("WITHDRAWAL");
        assertThat(bt.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(-100));
        assertThat(bt.getRelatedWithdrawalId()).isEqualTo(result.getId());
    }

    @Test
    void requestWithdrawal_shouldRecordWithdrawal() {
        Long workerId = 104L;
        setupApprovedRealName(workerId);
        setupBalance(workerId, BigDecimal.valueOf(500));

        withdrawalService.requestWithdrawal(workerId, BigDecimal.valueOf(100), "WECHAT", null);

        List<WithdrawalVO> history = withdrawalService.getWithdrawalHistory(workerId);
        assertThat(history).hasSize(1);
        assertThat(history.get(0).getAmount()).isEqualByComparingTo(BigDecimal.valueOf(100));
        assertThat(history.get(0).getStatus()).isEqualTo("COMPLETED");
    }

    @Test
    void requestWithdrawal_multipleWithdrawals_shouldWork() {
        Long workerId = 105L;
        setupApprovedRealName(workerId);
        setupBalance(workerId, BigDecimal.valueOf(1000));

        withdrawalService.requestWithdrawal(workerId, BigDecimal.valueOf(100), "WECHAT", null);
        withdrawalService.requestWithdrawal(workerId, BigDecimal.valueOf(200), "WECHAT", null);
        withdrawalService.requestWithdrawal(workerId, BigDecimal.valueOf(300), "WECHAT", null);

        List<WithdrawalVO> history = withdrawalService.getWithdrawalHistory(workerId);
        assertThat(history).hasSize(3);

        var balance = workerBalanceMapper.findByWorkerId(workerId);
        assertThat(balance.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(400));
    }

    @Test
    void requestWithdrawal_wechat_insufficientBalance_shouldThrow() {
        Long workerId = 106L;
        setupApprovedRealName(workerId);
        setupBalance(workerId, BigDecimal.valueOf(50));

        assertThatThrownBy(() -> withdrawalService.requestWithdrawal(workerId, BigDecimal.valueOf(100), "WECHAT", null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("余额不足");
    }

    @Test
    void requestWithdrawal_bankCard_insufficientBalance_shouldThrow() {
        Long workerId = 107L;
        setupApprovedRealName(workerId);
        setupBankCard(workerId);
        setupBalance(workerId, BigDecimal.valueOf(50));

        assertThatThrownBy(() -> withdrawalService.requestWithdrawal(workerId, BigDecimal.valueOf(100), "BANK_CARD", 1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("余额不足");
    }

    @Test
    void requestWithdrawal_belowMinimum_shouldThrow() {
        Long workerId = 108L;
        setupApprovedRealName(workerId);
        setupBalance(workerId, BigDecimal.valueOf(500));

        assertThatThrownBy(() -> withdrawalService.requestWithdrawal(workerId, new BigDecimal("0.5"), "WECHAT", null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("最低提现金额为1元");
    }

    @Test
    void requestWithdrawal_aboveMaximum_shouldThrow() {
        Long workerId = 109L;
        setupApprovedRealName(workerId);
        setupBalance(workerId, BigDecimal.valueOf(5000));

        assertThatThrownBy(() -> withdrawalService.requestWithdrawal(workerId, new BigDecimal("1001"), "WECHAT", null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("最高提现金额为1000元");
    }

    @Test
    void requestWithdrawal_realNameNotApproved_shouldThrow() {
        Long workerId = 110L;
        setupBalance(workerId, BigDecimal.valueOf(500));

        assertThatThrownBy(() -> withdrawalService.requestWithdrawal(workerId, BigDecimal.valueOf(100), "WECHAT", null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("请先完成实名认证");
    }

    @Test
    void requestWithdrawal_realNamePending_shouldThrow() {
        Long workerId = 111L;
        WorkerRealNameAuth auth = new WorkerRealNameAuth();
        auth.setWorkerId(workerId);
        auth.setStatus("PENDING");
        realNameAuthMapper.insert(auth);
        setupBalance(workerId, BigDecimal.valueOf(500));

        assertThatThrownBy(() -> withdrawalService.requestWithdrawal(workerId, BigDecimal.valueOf(100), "WECHAT", null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("请先完成实名认证");
    }

    @Test
    void requestWithdrawal_bankCard_noCard_shouldThrow() {
        Long workerId = 112L;
        setupApprovedRealName(workerId);
        setupBalance(workerId, BigDecimal.valueOf(500));

        assertThatThrownBy(() -> withdrawalService.requestWithdrawal(workerId, BigDecimal.valueOf(100), "BANK_CARD", 1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("请先绑定银行卡");
    }

    @Test
    void requestWithdrawal_unsupportedMethod_shouldThrow() {
        Long workerId = 113L;
        setupApprovedRealName(workerId);
        setupBalance(workerId, BigDecimal.valueOf(500));

        assertThatThrownBy(() -> withdrawalService.requestWithdrawal(workerId, BigDecimal.valueOf(100), "UNSUPPORTED", null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("不支持的提现方式");
    }

    @Test
    void getAvailableMethods_shouldReturnBothMethods() {
        List<?> methods = withdrawalService.getAvailableMethods(1L);
        assertThat(methods).hasSize(2);
    }

    @Test
    void getBankCards_withCard_shouldReturnCard() {
        Long workerId = 114L;
        setupBankCard(workerId);

        List<?> cards = withdrawalService.getBankCards(workerId);
        assertThat(cards).hasSize(1);
    }

    @Test
    void getBankCards_noCard_shouldReturnEmpty() {
        Long workerId = 115L;

        List<?> cards = withdrawalService.getBankCards(workerId);
        assertThat(cards).isEmpty();
    }

    @Test
    void getWithdrawalHistory_noRecords_shouldReturnEmpty() {
        List<WithdrawalVO> history = withdrawalService.getWithdrawalHistory(999L);
        assertThat(history).isEmpty();
    }
}
