package com.parttime.platform.service;

import com.parttime.platform.pojo.vo.WithdrawalRecordVO;
import com.parttime.platform.service.impl.WithdrawalRecordServiceImpl;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WithdrawalRecordServiceTest {

    private final WithdrawalRecordServiceImpl withdrawalRecordService = new WithdrawalRecordServiceImpl();

    @Test
    void list_shouldReturnList() {
        List<WithdrawalRecordVO> result = withdrawalRecordService.list("PENDING", null);
        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getWorkerName()).isNotNull();
    }

    @Test
    void detail_shouldReturnRecord() {
        WithdrawalRecordVO result = withdrawalRecordService.detail(1L);
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void approve_shouldNotThrow() {
        withdrawalRecordService.approve(1L);
    }

    @Test
    void reject_shouldNotThrow() {
        withdrawalRecordService.reject(1L, "reason");
    }
}
