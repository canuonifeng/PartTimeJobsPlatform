package com.parttime.platform.service;

import com.parttime.platform.exception.BusinessException;
import com.parttime.platform.mapper.SystemConfigMapper;
import com.parttime.platform.pojo.entity.SystemConfig;
import com.parttime.platform.pojo.vo.SystemConfigVO;
import com.parttime.platform.service.impl.SystemConfigServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SystemConfigServiceTest {

    @Mock
    private SystemConfigMapper systemConfigMapper;

    @InjectMocks
    private SystemConfigServiceImpl systemConfigService;

    @Test
    void getConfig_shouldReturnValue() {
        SystemConfig config = new SystemConfig();
        config.setConfigKey("platform_fee_rate");
        config.setConfigValue("0.10");

        when(systemConfigMapper.findByKey("platform_fee_rate")).thenReturn(Optional.of(config));

        String value = systemConfigService.getConfig("platform_fee_rate");

        assertThat(value).isEqualTo("0.10");
    }

    @Test
    void getConfig_notFound_shouldThrow() {
        when(systemConfigMapper.findByKey("nonexistent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> systemConfigService.getConfig("nonexistent"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void getAllConfigs_shouldReturnList() {
        SystemConfig config = new SystemConfig();
        config.setId(1L);
        config.setConfigKey("platform_fee_rate");
        config.setConfigValue("0.10");

        when(systemConfigMapper.findAll()).thenReturn(List.of(config));

        List<SystemConfigVO> result = systemConfigService.getAllConfigs();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getConfigKey()).isEqualTo("platform_fee_rate");
    }

    @Test
    void updateConfig_shouldUpdateAndReturn() {
        SystemConfig config = new SystemConfig();
        config.setId(1L);
        config.setConfigKey("platform_fee_rate");
        config.setConfigValue("0.10");

        when(systemConfigMapper.findByKey("platform_fee_rate")).thenReturn(Optional.of(config));

        SystemConfigVO response = systemConfigService.updateConfig("platform_fee_rate", "0.15");

        assertThat(response.getConfigValue()).isEqualTo("0.15");
        verify(systemConfigMapper).updateByKey("platform_fee_rate", "0.15");
    }

    @Test
    void updateConfig_notFound_shouldThrow() {
        when(systemConfigMapper.findByKey("nonexistent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> systemConfigService.updateConfig("nonexistent", "value"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("not found");
    }
}
