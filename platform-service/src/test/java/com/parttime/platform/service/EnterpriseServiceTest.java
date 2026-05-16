package com.parttime.platform.service;

import com.parttime.platform.mapper.CJobMapper;
import com.parttime.platform.mapper.EnterpriseMapper;
import com.parttime.platform.pojo.cmd.EnterpriseCreateCmd;
import com.parttime.platform.pojo.cmd.EnterpriseUpdateCmd;
import com.parttime.platform.pojo.entity.Enterprise;
import com.parttime.platform.pojo.vo.EnterpriseVO;
import com.parttime.platform.service.impl.EnterpriseServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnterpriseServiceTest {

    @Mock
    private EnterpriseMapper enterpriseMapper;

    @Mock
    private CJobMapper cJobMapper;

    @InjectMocks
    private EnterpriseServiceImpl enterpriseService;

    @Test
    void create_shouldPersistCompanyLogo() {
        EnterpriseCreateCmd cmd = new EnterpriseCreateCmd();
        cmd.setCompanyName("Test Enterprise");
        cmd.setContactName("Tester");
        cmd.setContactPhone("13800000000");
        cmd.setCompanyAddress("Shanghai");
        cmd.setBusinessLicense("BL-1");
        cmd.setCompanyLogo("https://cdn.example.com/logo.png");

        doAnswer(invocation -> {
            Enterprise enterprise = invocation.getArgument(0);
            enterprise.setId(1L);
            return 1;
        }).when(enterpriseMapper).insert(any(Enterprise.class));

        EnterpriseVO result = enterpriseService.create(cmd);

        assertThat(result.getCompanyLogo()).isEqualTo("https://cdn.example.com/logo.png");
        verify(enterpriseMapper).insert(any(Enterprise.class));
    }

    @Test
    void update_shouldPersistLogoAndSyncCJob() {
        Enterprise existing = new Enterprise();
        existing.setId(1L);
        existing.setCompanyName("Old Enterprise");
        existing.setCompanyLogo("https://cdn.example.com/old.png");

        EnterpriseUpdateCmd cmd = new EnterpriseUpdateCmd();
        cmd.setId(1L);
        cmd.setCompanyLogo("https://cdn.example.com/new.png");

        when(enterpriseMapper.findById(1L)).thenReturn(Optional.of(existing));

        EnterpriseVO result = enterpriseService.update(cmd);

        assertThat(result.getCompanyLogo()).isEqualTo("https://cdn.example.com/new.png");
        verify(enterpriseMapper).update(existing);
        verify(cJobMapper).updateCompanyLogoByCompanyId(1L, "https://cdn.example.com/new.png");
    }
}
