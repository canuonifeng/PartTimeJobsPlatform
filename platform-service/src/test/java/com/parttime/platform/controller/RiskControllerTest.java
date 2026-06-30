package com.parttime.platform.controller;

import com.parttime.platform.config.JwtTokenProvider;
import com.parttime.platform.config.SecurityConfig;
import com.parttime.platform.pojo.cmd.IdCmd;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RiskController.class)
@Import(SecurityConfig.class)
class RiskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private com.parttime.platform.service.RiskService riskService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "ADMIN")
    void getBlacklist_shouldReturnList() throws Exception {
        mockMvc.perform(post("/api/admin/risk/blacklist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getWhitelist_shouldReturnList() throws Exception {
        mockMvc.perform(post("/api/admin/risk/whitelist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void addToBlacklist_shouldSucceed() throws Exception {
        mockMvc.perform(post("/api/admin/risk/blacklist/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":1,\"reason\":\"test\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void removeFromBlacklist_shouldSucceed() throws Exception {
        IdCmd cmd = new IdCmd();
        cmd.setId(1L);

        mockMvc.perform(post("/api/admin/risk/blacklist/remove")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cmd)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getRiskRules_shouldReturnList() throws Exception {
        mockMvc.perform(post("/api/admin/risk/rules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void toggleRule_shouldSucceed() throws Exception {
        mockMvc.perform(post("/api/admin/risk/rules/toggle")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":1,\"enabled\":true}"))
                .andExpect(status().isOk());
    }
}
