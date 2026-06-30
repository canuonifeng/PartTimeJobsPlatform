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

@WebMvcTest(EnterpriseTopUpController.class)
@Import(SecurityConfig.class)
class EnterpriseTopUpControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private com.parttime.platform.service.EnterpriseTopUpService enterpriseTopUpService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "ADMIN")
    void listTopUpRecords_shouldReturnList() throws Exception {
        mockMvc.perform(post("/api/admin/top-up/list")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getTopUpDetail_shouldReturnRecord() throws Exception {
        IdCmd cmd = new IdCmd();
        cmd.setId(1L);

        mockMvc.perform(post("/api/admin/top-up/detail")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cmd)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void approveTopUp_shouldSucceed() throws Exception {
        mockMvc.perform(post("/api/admin/top-up/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":1,\"remark\":\"approved\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void rejectTopUp_shouldSucceed() throws Exception {
        mockMvc.perform(post("/api/admin/top-up/reject")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":1,\"remark\":\"rejected\"}"))
                .andExpect(status().isOk());
    }
}
