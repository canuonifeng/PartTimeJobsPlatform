package com.parttime.platform.controller;

import com.parttime.platform.config.JwtTokenProvider;
import com.parttime.platform.config.SecurityConfig;
import com.parttime.platform.pojo.cmd.IdCmd;
import com.parttime.platform.pojo.cmd.JobQueryCmd;
import com.parttime.platform.pojo.vo.JobVO;
import com.parttime.platform.service.JobService;
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
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(JobController.class)
@Import(SecurityConfig.class)
class JobControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JobService jobService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "ADMIN")
    void listJobs_shouldReturnJobList() throws Exception {
        JobVO job = new JobVO();
        job.setId(1L);
        job.setTitle("Test Job");
        job.setStatus("ACTIVE");

        when(jobService.list(any(JobQueryCmd.class))).thenReturn(List.of(job));

        mockMvc.perform(post("/api/admin/jobs/list")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].title").value("Test Job"));

        verify(jobService).list(any(JobQueryCmd.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void listJobs_withStatusFilter() throws Exception {
        JobQueryCmd cmd = new JobQueryCmd();
        cmd.setStatus("ACTIVE");

        JobVO job = new JobVO();
        job.setId(1L);
        job.setTitle("Active Job");
        job.setStatus("ACTIVE");

        when(jobService.list(any(JobQueryCmd.class))).thenReturn(List.of(job));

        mockMvc.perform(post("/api/admin/jobs/list")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cmd)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].status").value("ACTIVE"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getJobDetail_shouldReturnJob() throws Exception {
        JobVO job = new JobVO();
        job.setId(1L);
        job.setTitle("Detail Job");
        job.setContactName("John Doe");

        when(jobService.detail(1L)).thenReturn(job);

        IdCmd cmd = new IdCmd();
        cmd.setId(1L);

        mockMvc.perform(post("/api/admin/jobs/detail")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cmd)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Detail Job"));

        verify(jobService).detail(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void closeJob_shouldSucceed() throws Exception {
        IdCmd cmd = new IdCmd();
        cmd.setId(1L);

        mockMvc.perform(post("/api/admin/jobs/close")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cmd)))
                .andExpect(status().isOk());

        verify(jobService).closeJob(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void reopenJob_shouldSucceed() throws Exception {
        IdCmd cmd = new IdCmd();
        cmd.setId(1L);

        mockMvc.perform(post("/api/admin/jobs/reopen")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cmd)))
                .andExpect(status().isOk());

        verify(jobService).reopenJob(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void setJobTop_shouldUpdateStatus() throws Exception {
        Map<String, Object> body = Map.of("id", 1L, "isTop", true);

        mockMvc.perform(post("/api/admin/jobs/set-top")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());

        verify(jobService).setTop(eq(1L), eq(true));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void setJobRecommended_shouldUpdateStatus() throws Exception {
        Map<String, Object> body = Map.of("id", 1L, "isRecommended", true);

        mockMvc.perform(post("/api/admin/jobs/set-recommended")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());

        verify(jobService).setRecommended(eq(1L), eq(true));
    }
}
