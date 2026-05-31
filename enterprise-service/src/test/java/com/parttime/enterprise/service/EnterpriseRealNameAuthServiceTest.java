package com.parttime.enterprise.service;

import com.parttime.enterprise.mapper.EnterpriseRealNameAuthMapper;
import com.parttime.enterprise.pojo.cmd.EnterpriseRealNameSubmitCmd;
import com.parttime.enterprise.pojo.entity.EnterpriseRealNameAuth;
import com.parttime.enterprise.pojo.vo.EnterpriseRealNameAuthVO;
import com.parttime.enterprise.service.impl.EnterpriseRealNameAuthServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnterpriseRealNameAuthServiceTest {

    @Mock
    private EnterpriseRealNameAuthMapper mapper;

    @InjectMocks
    private EnterpriseRealNameAuthServiceImpl service;

    private EnterpriseRealNameSubmitCmd validCmd() {
        EnterpriseRealNameSubmitCmd cmd = new EnterpriseRealNameSubmitCmd();
        cmd.setLegalPersonName("法人甲");
        cmd.setLegalPersonIdCard("110101199001011234");
        cmd.setUnifiedSocialCreditCode("91110000XXXXXXXXXX");
        cmd.setBusinessLicenseUrl("http://files/license.jpg");
        return cmd;
    }

    @Test
    void submit_shouldInsertWhenNoRecord() {
        when(mapper.findByEnterpriseId(1L)).thenReturn(Optional.empty());

        EnterpriseRealNameAuthVO vo = service.submit(1L, validCmd());

        assertThat(vo.getStatus()).isEqualTo("PENDING");
        verify(mapper).insert(org.mockito.ArgumentMatchers.any(EnterpriseRealNameAuth.class));
    }

    @Test
    void submit_shouldRejectWhenPending() {
        EnterpriseRealNameAuth existing = new EnterpriseRealNameAuth();
        existing.setStatus("PENDING");
        when(mapper.findByEnterpriseId(1L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.submit(1L, validCmd()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("已提交");
    }

    @Test
    void submit_shouldRejectWhenApproved() {
        EnterpriseRealNameAuth existing = new EnterpriseRealNameAuth();
        existing.setStatus("APPROVED");
        when(mapper.findByEnterpriseId(1L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.submit(1L, validCmd()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("已通过");
    }

    @Test
    void submit_shouldResubmitWhenRejected() {
        EnterpriseRealNameAuth existing = new EnterpriseRealNameAuth();
        existing.setEnterpriseId(1L);
        existing.setStatus("REJECTED");
        existing.setRejectReason("照片不清晰");
        when(mapper.findByEnterpriseId(1L)).thenReturn(Optional.of(existing));

        EnterpriseRealNameAuthVO vo = service.submit(1L, validCmd());

        assertThat(vo.getStatus()).isEqualTo("PENDING");
        assertThat(vo.getRejectReason()).isNull();
        verify(mapper).update(org.mockito.ArgumentMatchers.any(EnterpriseRealNameAuth.class));
    }

    @Test
    void submit_shouldRejectIncompleteCmd() {
        EnterpriseRealNameSubmitCmd cmd = new EnterpriseRealNameSubmitCmd();
        cmd.setLegalPersonName("法人甲");

        assertThatThrownBy(() -> service.submit(1L, cmd))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("必填");
    }

    @Test
    void getStatus_shouldReturnNoneWhenMissing() {
        when(mapper.findByEnterpriseId(99L)).thenReturn(Optional.empty());

        EnterpriseRealNameAuthVO vo = service.getStatus(99L);
        assertThat(vo.getStatus()).isEqualTo("NONE");
    }

    @Test
    void getStatus_shouldReturnExistingStatus() {
        EnterpriseRealNameAuth existing = new EnterpriseRealNameAuth();
        existing.setStatus("APPROVED");
        existing.setLegalPersonName("法人甲");
        existing.setLegalPersonIdCard("110101199001011234");
        when(mapper.findByEnterpriseId(1L)).thenReturn(Optional.of(existing));

        EnterpriseRealNameAuthVO vo = service.getStatus(1L);
        assertThat(vo.getStatus()).isEqualTo("APPROVED");
        assertThat(vo.getLegalPersonIdCardMasked()).contains("****");
    }
}
