package com.parttime.cservice.service;

import com.parttime.cservice.pojo.cmd.WorkerBankCardCmd;
import com.parttime.cservice.pojo.entity.WorkerBankCard;
import com.parttime.cservice.service.impl.WorkerBankCardServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WorkerBankCardServiceTest {

    @InjectMocks
    private WorkerBankCardServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(service, "workerBankCardMapper", InMemoryMappers.createWorkerBankCardMapper());
    }

    private WorkerBankCardCmd validCmd() {
        WorkerBankCardCmd cmd = new WorkerBankCardCmd();
        cmd.setCardHolder("Zhang");
        cmd.setCardNumber("6222021234567890");
        cmd.setBankName("ICBC");
        cmd.setBankBranch("Beijing");
        return cmd;
    }

    @Test
    void get_returnsNullWhenNotFound() {
        assertThat(service.get(1L)).isNull();
    }

    @Test
    void upsert_insertsWhenNotExists() {
        WorkerBankCard saved = service.upsert(1L, validCmd());
        assertThat(saved.getWorkerId()).isEqualTo(1L);
        assertThat(saved.getCardNumber()).isEqualTo("6222021234567890");
        assertThat(service.get(1L)).isNotNull();
    }

    @Test
    void upsert_updatesWhenExists() {
        service.upsert(1L, validCmd());
        WorkerBankCardCmd cmd2 = validCmd();
        cmd2.setBankName("CCB");
        service.upsert(1L, cmd2);
        assertThat(service.get(1L).getBankName()).isEqualTo("CCB");
    }

    @Test
    void upsert_throwsWhenCardHolderMissing() {
        WorkerBankCardCmd cmd = validCmd();
        cmd.setCardHolder(null);
        assertThatThrownBy(() -> service.upsert(1L, cmd))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void upsert_throwsWhenCardNumberMissing() {
        WorkerBankCardCmd cmd = validCmd();
        cmd.setCardNumber("");
        assertThatThrownBy(() -> service.upsert(1L, cmd))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void delete_removesCard() {
        service.upsert(1L, validCmd());
        service.delete(1L);
        assertThat(service.get(1L)).isNull();
    }
}
