package com.parttime.platform.web.controller;

import com.parttime.platform.api.dto.SystemConfigResponse;
import com.parttime.platform.api.dto.SystemConfigUpdateRequest;
import com.parttime.platform.core.auth.JwtTokenProvider;
import com.parttime.platform.core.service.SystemConfigService;
import com.parttime.platform.web.config.SecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SystemConfigController.class)
@Import(SecurityConfig.class)
class SystemConfigControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SystemConfigService systemConfigService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "ADMIN")
    void listConfigs_shouldReturnList() throws Exception {
        SystemConfigResponse config = new SystemConfigResponse();
        config.setId(1L);
        config.setConfigKey("platform_fee_rate");
        config.setConfigValue("0.10");

        when(systemConfigService.getAllConfigs()).thenReturn(List.of(config));

        mockMvc.perform(get("/api/admin/configs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].configKey").value("platform_fee_rate"));

        verify(systemConfigService).getAllConfigs();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateConfig_shouldReturnUpdated() throws Exception {
        SystemConfigResponse response = new SystemConfigResponse();
        response.setId(1L);
        response.setConfigKey("platform_fee_rate");
        response.setConfigValue("0.15");

        when(systemConfigService.updateConfig(eq("platform_fee_rate"), anyString())).thenReturn(response);

        String json = "{\"value\":\"0.15\"}";

        mockMvc.perform(put("/api/admin/configs/platform_fee_rate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.configValue").value("0.15"));

        verify(systemConfigService).updateConfig("platform_fee_rate", "0.15");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateConfig_missingKey_shouldReturnBadRequest() throws Exception {
        when(systemConfigService.updateConfig(eq("nonexistent"), anyString()))
                .thenThrow(new com.parttime.platform.core.exception.BusinessException("Config not found: nonexistent"));

        String json = "{\"value\":\"test\"}";

        mockMvc.perform(put("/api/admin/configs/nonexistent")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

}
