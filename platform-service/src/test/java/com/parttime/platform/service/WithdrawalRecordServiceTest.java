package com.parttime.platform.service;

import com.parttime.platform.mapper.WithdrawalRecordMapper;
import com.parttime.platform.mapper.WorkerMapper;
import com.parttime.platform.pojo.entity.WithdrawalRecord;
import com.parttime.platform.pojo.entity.Worker;
import com.parttime.platform.pojo.vo.PageVO;
import com.parttime.platform.pojo.vo.WithdrawalRecordVO;
import com.parttime.platform.service.impl.WithdrawalRecordServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WithdrawalRecordServiceTest {

    @Mock
    private WithdrawalRecordMapper withdrawalRecordMapper;

    @Mock
    private WorkerMapper workerMapper;

    @InjectMocks
    private WithdrawalRecordServiceImpl withdrawalRecordService;

    @Test
    void listRecords_shouldReturnPagedWithdrawalRecords() {
        LocalDateTime start = LocalDateTime.of(2026, 5, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 5, 31, 23, 59, 59);
        WithdrawalRecord record = new WithdrawalRecord();
        record.setId(1L);
        record.setWorkerId(2L);
        record.setWorkerName("张三");
        record.setAmount(new BigDecimal("100.00"));
        record.setStatus("COMPLETED");
        record.setThirdPartySerialNo("WTHD123");
        record.setThirdPartyPlatform("SIMULATED_PAY");

        Worker worker = new Worker();
        worker.setId(2L);
        worker.setName("张三");
        worker.setPhone("13800138000");

        when(withdrawalRecordMapper.findPage(2L, "COMPLETED", start, end, 20, 20)).thenReturn(List.of(record));
        when(withdrawalRecordMapper.countPage(2L, "COMPLETED", start, end)).thenReturn(1L);
        when(workerMapper.findByIds(anyList())).thenReturn(List.of(worker));

        PageVO<WithdrawalRecordVO> result = withdrawalRecordService.listRecords(2L, "COMPLETED", start, end, 2, 20);

        assertThat(result.getTotal()).isEqualTo(1L);
        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getRecords().get(0).getWorkerName()).isEqualTo("张三");
        assertThat(result.getRecords().get(0).getThirdPartySerialNo()).isEqualTo("WTHD123");
        verify(withdrawalRecordMapper).findPage(2L, "COMPLETED", start, end, 20, 20);
    }
}
