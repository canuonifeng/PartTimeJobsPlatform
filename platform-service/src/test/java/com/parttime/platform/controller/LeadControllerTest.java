package com.parttime.platform.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.parttime.platform.config.JwtTokenProvider;
import com.parttime.platform.config.SecurityConfig;
import com.parttime.platform.pojo.cmd.LeadCreateCmd;
import com.parttime.platform.pojo.vo.LeadVO;
import com.parttime.platform.service.LeadService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LeadController.class)
@Import(SecurityConfig.class)
class LeadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LeadService leadService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createLead_shouldAcceptPublicRequest() throws Exception {
        LeadCreateCmd cmd = new LeadCreateCmd();
        cmd.setContactName("张三");
        cmd.setCompanyName("上海零售有限公司");
        cmd.setPhone("13800138000");
        cmd.setDemand("希望了解多门店排班和薪资结算");

        LeadVO response = new LeadVO();
        response.setId(12L);
        response.setContactName("张三");
        response.setCompanyName("上海零售有限公司");
        response.setPhone("13800138000");
        response.setStatus("NEW");

        when(leadService.createLead(any(LeadCreateCmd.class))).thenReturn(response);

        mockMvc.perform(post("/api/leads")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cmd)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(12))
                .andExpect(jsonPath("$.data.status").value("NEW"));

        verify(leadService).createLead(any(LeadCreateCmd.class));
    }
}
