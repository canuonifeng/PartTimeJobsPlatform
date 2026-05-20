package com.parttime.enterprise.controller;

import com.parttime.enterprise.enums.ApplicationStatus;
import com.parttime.enterprise.exception.BusinessException;
import com.parttime.enterprise.exception.GlobalExceptionHandler;
import com.parttime.enterprise.pojo.vo.JobApplicationVO;
import com.parttime.enterprise.service.ApplicationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ApplicationControllerTest {

    @Mock
    private ApplicationService applicationService;

    @InjectMocks
    private ApplicationController applicationController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(applicationController, "applicationService", applicationService);
        mockMvc = MockMvcBuilders.standaloneSetup(applicationController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getApplicationsByJob_shouldReturn200() throws Exception {
        JobApplicationVO app = new JobApplicationVO();
        app.setId(1L);
        app.setJobId(100L);
        app.setWorkerId(10L);
        app.setWorkerPhone("13800000000");
        app.setStatus(ApplicationStatus.PENDING);
        app.setAppliedAt(LocalDateTime.of(2026, 5, 1, 10, 0));

        when(applicationService.getApplicationsByJob(100L, null, null, 1, 20)).thenReturn(List.of(app));

        mockMvc.perform(get("/api/applications").param("jobId", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].workerPhone").value("13800000000"))
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    void getApplicationsByJob_shouldSupportPageParams() throws Exception {
        JobApplicationVO app = new JobApplicationVO();
        app.setId(1L);
        app.setJobId(100L);
        app.setWorkerId(10L);
        app.setStatus(ApplicationStatus.PENDING);
        app.setAppliedAt(LocalDateTime.of(2026, 5, 1, 10, 0));

        when(applicationService.getApplicationsByJob(100L, null, null, 2, 20)).thenReturn(List.of(app));

        mockMvc.perform(get("/api/applications").param("jobId", "100").param("page", "2").param("pageSize", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    void acceptApplication_shouldReturn200() throws Exception {
        JobApplicationVO app = new JobApplicationVO();
        app.setId(1L);
        app.setJobId(100L);
        app.setWorkerId(10L);
        app.setStatus(ApplicationStatus.ACCEPTED);

        when(applicationService.acceptApplication(1L)).thenReturn(app);

        mockMvc.perform(put("/api/applications/accept").param("applicationId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"));
    }

    @Test
    void rejectApplication_shouldReturn200() throws Exception {
        JobApplicationVO app = new JobApplicationVO();
        app.setId(1L);
        app.setJobId(100L);
        app.setWorkerId(10L);
        app.setStatus(ApplicationStatus.REJECTED);

        when(applicationService.rejectApplication(1L)).thenReturn(app);

        mockMvc.perform(put("/api/applications/reject").param("applicationId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));
    }

    @Test
    void acceptApplicationOnFullJob_shouldReturn400() throws Exception {
        when(applicationService.acceptApplication(anyLong()))
                .thenThrow(new BusinessException("岗位已录满"));

        mockMvc.perform(put("/api/applications/accept").param("applicationId", "1"))
                .andExpect(status().isBadRequest());
    }
}
