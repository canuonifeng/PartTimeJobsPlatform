package com.parttime.platform.controller;

import com.parttime.platform.config.JwtTokenProvider;
import com.parttime.platform.config.SecurityConfig;
import com.parttime.platform.pojo.vo.JobReportVO;
import com.parttime.platform.service.JobReportService;
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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(JobReportController.class)
@Import(SecurityConfig.class)
class JobReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JobReportService jobReportService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "ADMIN")
    void listJobReports_shouldReturnList() throws Exception {
        when(jobReportService.list(null)).thenReturn(List.of());

        mockMvc.perform(post("/api/admin/job-reports/list").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk());

        verify(jobReportService).list(null);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void listJobReports_withStatus_shouldReturnFiltered() throws Exception {
        when(jobReportService.list("PENDING")).thenReturn(List.of());

        mockMvc.perform(post("/api/admin/job-reports/list").contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"PENDING\"}"))
                .andExpect(status().isOk());

        verify(jobReportService).list("PENDING");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getJobReport_shouldReturnDetail() throws Exception {
        JobReportVO response = new JobReportVO();
        response.setId(1L);
        response.setReason("Spam");

        when(jobReportService.detail(1L)).thenReturn(response);

        mockMvc.perform(post("/api/admin/job-reports/detail").contentType(MediaType.APPLICATION_JSON).content("{\"id\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.reason").value("Spam"));

        verify(jobReportService).detail(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void dismissReport_shouldReturnOk() throws Exception {
        mockMvc.perform(post("/api/admin/job-reports/dismiss").contentType(MediaType.APPLICATION_JSON).content("{\"id\":1}"))
                .andExpect(status().isOk());

        verify(jobReportService).dismiss(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void banReport_shouldReturnOk() throws Exception {
        mockMvc.perform(post("/api/admin/job-reports/ban").contentType(MediaType.APPLICATION_JSON).content("{\"id\":1}"))
                .andExpect(status().isOk());

        verify(jobReportService).ban(1L);
    }

    @Test
    void listJobReports_withoutAuth_shouldReturn401() throws Exception {
        mockMvc.perform(post("/api/admin/job-reports/list").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());
    }
}
