package com.parttime.enterprise.service;

import com.parttime.enterprise.mapper.EnterpriseMapper;
import com.parttime.enterprise.service.impl.EnterpriseServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnterpriseServiceTest {

    @Mock
    private EnterpriseMapper enterpriseMapper;

    @InjectMocks
    private EnterpriseServiceImpl enterpriseService;

    @Test
    void getEnterpriseInfo_shouldReturnMap() {
        when(enterpriseMapper.findCompanyNameById(100L)).thenReturn("Acme Corp");
        when(enterpriseMapper.findCompanyLogoById(100L)).thenReturn("https://logo.png");

        Map<String, Object> result = enterpriseService.getEnterpriseInfo(100L);

        assertThat(result).containsEntry("id", 100L);
        assertThat(result).containsEntry("companyName", "Acme Corp");
        assertThat(result).containsEntry("companyLogo", "https://logo.png");
    }
}
