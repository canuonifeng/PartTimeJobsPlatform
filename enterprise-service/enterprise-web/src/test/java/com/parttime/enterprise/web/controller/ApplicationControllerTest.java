package com.parttime.enterprise.web.controller;

import com.parttime.enterprise.api.dto.ApplicationStatus;
import com.parttime.enterprise.api.dto.JobApplicationResponse;
import com.parttime.enterprise.core.exception.BusinessException;
import com.parttime.enterprise.core.service.ApplicationService;
import com.parttime.enterprise.web.config.SecurityConfig;
import com.parttime.enterprise.core.auth.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ApplicationController.class)
@Import(SecurityConfig.class)
class ApplicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ApplicationService applicationService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @WithMockUser(roles = "ADMIN")
    void getApplicationsByJob_shouldReturn200() throws Exception {
        JobApplicationResponse app = new JobApplicationResponse();
        app.setId(1L);
        app.setJobId(100L);
        app.setWorkerId(10L);
        app.setStatus(ApplicationStatus.PENDING);
        app.setAppliedAt(LocalDateTime.of(2026, 5, 1, 10, 0));

        when(applicationService.getApplicationsByJob(100L)).thenReturn(List.of(app));

        mockMvc.perform(get("/api/jobs/100/applications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void acceptApplication_shouldReturn200() throws Exception {
        JobApplicationResponse app = new JobApplicationResponse();
        app.setId(1L);
        app.setJobId(100L);
        app.setWorkerId(10L);
        app.setStatus(ApplicationStatus.ACCEPTED);

        when(applicationService.acceptApplication(1L)).thenReturn(app);

        mockMvc.perform(put("/api/jobs/100/applications/1/accept"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void rejectApplication_shouldReturn200() throws Exception {
        JobApplicationResponse app = new JobApplicationResponse();
        app.setId(1L);
        app.setJobId(100L);
        app.setWorkerId(10L);
        app.setStatus(ApplicationStatus.REJECTED);

        when(applicationService.rejectApplication(1L)).thenReturn(app);

        mockMvc.perform(put("/api/jobs/100/applications/1/reject"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void acceptApplicationOnFullJob_shouldReturn400() throws Exception {
        when(applicationService.acceptApplication(anyLong()))
                .thenThrow(new BusinessException("岗位已录满"));

        mockMvc.perform(put("/api/jobs/100/applications/1/accept"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("岗位已录满"));
    }
}
