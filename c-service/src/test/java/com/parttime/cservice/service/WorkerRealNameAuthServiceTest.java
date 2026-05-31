package com.parttime.cservice.service;

import com.parttime.cservice.pojo.cmd.WorkerRealNameSubmitCmd;
import com.parttime.cservice.pojo.entity.WorkerRealNameAuth;
import com.parttime.cservice.pojo.vo.WorkerRealNameAuthVO;
import com.parttime.cservice.mapper.WorkerRealNameAuthMapper;
import com.parttime.cservice.service.impl.WorkerRealNameAuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WorkerRealNameAuthServiceTest {

    @InjectMocks
    private WorkerRealNameAuthServiceImpl service;

    private WorkerRealNameAuthMapper mapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mapper = InMemoryMappers.createWorkerRealNameAuthMapper();
        ReflectionTestUtils.setField(service, "workerRealNameAuthMapper", mapper);
    }

    @Test
    void submit_shouldInsertPendingForNewWorker() {
        WorkerRealNameSubmitCmd cmd = new WorkerRealNameSubmitCmd();
        cmd.setRealName("张三");
        cmd.setIdCardNo("110101199001011234");

        WorkerRealNameAuthVO vo = service.submit(1L, cmd);

        assertThat(vo.getStatus()).isEqualTo("PENDING");
        assertThat(vo.getRealName()).isEqualTo("张三");
        assertThat(vo.getIdCardNoMasked()).contains("****");
    }

    @Test
    void submit_shouldRejectWhenPending() {
        WorkerRealNameSubmitCmd cmd = new WorkerRealNameSubmitCmd();
        cmd.setRealName("张三");
        cmd.setIdCardNo("110101199001011234");
        service.submit(1L, cmd);

        assertThatThrownBy(() -> service.submit(1L, cmd))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("已提交");
    }

    @Test
    void submit_shouldRejectWhenApproved() {
        WorkerRealNameSubmitCmd cmd = new WorkerRealNameSubmitCmd();
        cmd.setRealName("张三");
        cmd.setIdCardNo("110101199001011234");
        service.submit(1L, cmd);

        WorkerRealNameAuth current = mapper.findByWorkerId(1L).get();
        current.setStatus("APPROVED");
        current.setReviewedAt(LocalDateTime.now());
        mapper.update(current);

        assertThatThrownBy(() -> service.submit(1L, cmd))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("已通过");
    }

    @Test
    void submit_shouldResubmitWhenRejected() {
        WorkerRealNameSubmitCmd cmd = new WorkerRealNameSubmitCmd();
        cmd.setRealName("张三");
        cmd.setIdCardNo("110101199001011234");
        service.submit(1L, cmd);

        WorkerRealNameAuth current = mapper.findByWorkerId(1L).get();
        current.setStatus("REJECTED");
        current.setRejectReason("照片不清晰");
        mapper.update(current);

        WorkerRealNameSubmitCmd cmd2 = new WorkerRealNameSubmitCmd();
        cmd2.setRealName("张三");
        cmd2.setIdCardNo("110101199001015678");

        WorkerRealNameAuthVO vo = service.submit(1L, cmd2);

        assertThat(vo.getStatus()).isEqualTo("PENDING");
        assertThat(vo.getRejectReason()).isNull();
    }

    @Test
    void getStatus_shouldReturnNoneWhenMissing() {
        WorkerRealNameAuthVO vo = service.getStatus(999L);
        assertThat(vo.getStatus()).isEqualTo("NONE");
    }

    @Test
    void getStatus_shouldReturnPendingWhenSubmitted() {
        WorkerRealNameSubmitCmd cmd = new WorkerRealNameSubmitCmd();
        cmd.setRealName("张三");
        cmd.setIdCardNo("110101199001011234");
        service.submit(1L, cmd);

        WorkerRealNameAuthVO vo = service.getStatus(1L);
        assertThat(vo.getStatus()).isEqualTo("PENDING");
    }

    @Test
    void submit_shouldRejectWhenRealNameMissing() {
        WorkerRealNameSubmitCmd cmd = new WorkerRealNameSubmitCmd();
        cmd.setIdCardNo("110101199001011234");

        assertThatThrownBy(() -> service.submit(1L, cmd))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("必填");
    }
}
