package com.parttime.cservice.service;

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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WithdrawalServiceTest {

    @InjectMocks
    private WithdrawalServiceImpl withdrawalService;

    private WorkerRealNameAuthMapper realNameAuthMapper;
    private WorkerBankCardMapper bankCardMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        realNameAuthMapper = InMemoryMappers.createWorkerRealNameAuthMapper();
        bankCardMapper = InMemoryMappers.createWorkerBankCardMapper();
        ReflectionTestUtils.setField(withdrawalService, "withdrawalRecordMapper", InMemoryMappers.createWithdrawalRecordMapper());
        ReflectionTestUtils.setField(withdrawalService, "workerRealNameAuthMapper", realNameAuthMapper);
        ReflectionTestUtils.setField(withdrawalService, "workerBankCardMapper", bankCardMapper);
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
        assertThatThrownBy(() -> withdrawalService.requestWithdrawal(1L, new BigDecimal("100")))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void requestWithdrawal_shouldThrowWhenRealNamePending() {
        WorkerRealNameAuth auth = new WorkerRealNameAuth();
        auth.setWorkerId(1L);
        auth.setStatus("PENDING");
        realNameAuthMapper.insert(auth);
        assertThatThrownBy(() -> withdrawalService.requestWithdrawal(1L, new BigDecimal("100")))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void requestWithdrawal_shouldThrowWhenNoBankCard() {
        setupApprovedRealName(1L);
        assertThatThrownBy(() -> withdrawalService.requestWithdrawal(1L, new BigDecimal("100")))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void requestWithdrawal_shouldThrowWhenAmountIsNull() {
        assertThatThrownBy(() -> withdrawalService.requestWithdrawal(1L, null))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void requestWithdrawal_shouldThrowWhenAmountIsZero() {
        assertThatThrownBy(() -> withdrawalService.requestWithdrawal(1L, BigDecimal.ZERO))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void getWithdrawalHistory_shouldReturnEmptyForNoRecords() {
        List<WithdrawalVO> history = withdrawalService.getWithdrawalHistory(999L);
        assertThat(history).isEmpty();
    }
}
