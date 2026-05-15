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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
        when(jobReportService.getJobReports(null)).thenReturn(List.of());

        mockMvc.perform(get("/api/job-reports"))
                .andExpect(status().isOk());

        verify(jobReportService).getJobReports(null);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void listJobReports_withStatus_shouldReturnFiltered() throws Exception {
        when(jobReportService.getJobReports("PENDING")).thenReturn(List.of());

        mockMvc.perform(get("/api/job-reports?status=PENDING"))
                .andExpect(status().isOk());

        verify(jobReportService).getJobReports("PENDING");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getJobReport_shouldReturnDetail() throws Exception {
        JobReportVO response = new JobReportVO();
        response.setId(1L);
        response.setJobId(100L);
        response.setReason("Spam");

        when(jobReportService.getJobReport(1L)).thenReturn(response);

        mockMvc.perform(get("/api/job-reports/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reason").value("Spam"));

        verify(jobReportService).getJobReport(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void dismissReport_shouldReturnOk() throws Exception {
        JobReportVO response = new JobReportVO();
        response.setId(1L);
        response.setStatus("DISMISSED");

        when(jobReportService.dismissReport(eq(1L), any(), any())).thenReturn(response);

        String json = "{\"remark\":\"No violation\"}";

        mockMvc.perform(put("/api/job-reports/1/dismiss")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DISMISSED"));

        verify(jobReportService).dismissReport(eq(1L), any(), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void banReport_shouldReturnOk() throws Exception {
        JobReportVO response = new JobReportVO();
        response.setId(1L);
        response.setStatus("BANNED");

        when(jobReportService.banJobReport(eq(1L), any(), any())).thenReturn(response);

        String json = "{\"remark\":\"Violates terms\"}";

        mockMvc.perform(put("/api/job-reports/1/ban")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("BANNED"));

        verify(jobReportService).banJobReport(eq(1L), any(), any());
    }

    @Test
    void listJobReports_withoutAuth_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/job-reports"))
                .andExpect(status().isUnauthorized());
    }
}
