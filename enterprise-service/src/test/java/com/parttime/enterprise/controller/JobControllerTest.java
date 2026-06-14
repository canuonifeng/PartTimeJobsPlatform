package com.parttime.enterprise.controller;

import com.parttime.enterprise.enums.JobRateType;
import com.parttime.enterprise.enums.JobStatus;
import com.parttime.enterprise.pojo.cmd.JobRateCmd;
import com.parttime.enterprise.pojo.cmd.JobScheduleCmd;
import com.parttime.enterprise.pojo.vo.JobRateVO;
import com.parttime.enterprise.pojo.vo.JobScheduleVO;
import com.parttime.enterprise.pojo.vo.JobVO;
import com.parttime.enterprise.service.JobService;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class JobControllerTest {

    @Mock
    private JobService jobService;

    @InjectMocks
    private JobController jobController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(jobController).build();
    }

    @Test
    void publishJob_shouldReturnOk() throws Exception {
        JobVO response = new JobVO();
        response.setId(1L);
        response.setStatus(JobStatus.PUBLISHED);

        when(jobService.publishJob(1L)).thenReturn(response);

        mockMvc.perform(post("/api/jobs/publish")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PUBLISHED"));

        verify(jobService).publishJob(1L);
    }

    @Test
    void closeJob_shouldReturnOk() throws Exception {
        JobVO response = new JobVO();
        response.setId(1L);
        response.setStatus(JobStatus.CLOSED);

        when(jobService.closeJob(1L)).thenReturn(response);

        mockMvc.perform(post("/api/jobs/close")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CLOSED"));

        verify(jobService).closeJob(1L);
    }

    @Test
    void reopenJob_shouldReturnOk() throws Exception {
        JobVO response = new JobVO();
        response.setId(1L);
        response.setStatus(JobStatus.PUBLISHED);

        when(jobService.reopenJob(1L)).thenReturn(response);

        mockMvc.perform(post("/api/jobs/reopen")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PUBLISHED"));

        verify(jobService).reopenJob(1L);
    }

    @Test
    void getJobRates_shouldReturnRates() throws Exception {
        JobRateVO rate = new JobRateVO();
        rate.setId(1L);
        rate.setJobId(100L);
        rate.setType(JobRateType.HOURLY);
        rate.setAmount(new BigDecimal("25.00"));

        when(jobService.getJobRates(100L)).thenReturn(List.of(rate));

        mockMvc.perform(get("/api/jobs/rates").param("jobId", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].type").value("HOURLY"))
                .andExpect(jsonPath("$.data[0].amount").value(25.00));

        verify(jobService).getJobRates(100L);
    }

    @Test
    void addJobRate_shouldReturnCreated() throws Exception {
        JobRateCmd request = new JobRateCmd();
        request.setType(JobRateType.HOURLY);
        request.setAmount(new BigDecimal("25.00"));
        request.setCurrency("CNY");

        JobRateVO response = new JobRateVO();
        response.setId(1L);
        response.setJobId(100L);
        response.setType(JobRateType.HOURLY);
        response.setAmount(new BigDecimal("25.00"));

        when(jobService.addJobRate(eq(100L), any(JobRateCmd.class))).thenReturn(response);

        request.setJobId(100L);

        mockMvc.perform(post("/api/jobs/rates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.type").value("HOURLY"));

        verify(jobService).addJobRate(eq(100L), any(JobRateCmd.class));
    }

    @Test
    void updateJobRate_shouldReturnOk() throws Exception {
        JobRateCmd request = new JobRateCmd();
        request.setType(JobRateType.DAILY);
        request.setAmount(new BigDecimal("300.00"));

        JobRateVO response = new JobRateVO();
        response.setId(1L);
        response.setType(JobRateType.DAILY);
        response.setAmount(new BigDecimal("300.00"));

        when(jobService.updateJobRate(eq(1L), any(JobRateCmd.class))).thenReturn(response);

        request.setRateId(1L);

        mockMvc.perform(post("/api/jobs/rates/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.type").value("DAILY"));
    }

    @Test
    void removeJobRate_shouldReturnNoContent() throws Exception {
        mockMvc.perform(post("/api/jobs/rates/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rateId\":1}"))
                .andExpect(status().isOk());

        verify(jobService).removeJobRate(1L);
    }

    @Test
    void getJobSchedules_shouldReturnSchedules() throws Exception {
        JobScheduleVO schedule = new JobScheduleVO();
        schedule.setId(1L);
        schedule.setJobId(100L);
        schedule.setScheduleDate(LocalDate.of(2026, 6, 1));
        schedule.setStartTime(LocalTime.of(9, 0));
        schedule.setEndTime(LocalTime.of(18, 0));
        schedule.setSlotsAvailable(5);

        when(jobService.getJobSchedules(100L)).thenReturn(List.of(schedule));

        mockMvc.perform(get("/api/jobs/schedules").param("jobId", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].slotsAvailable").value(5));

        verify(jobService).getJobSchedules(100L);
    }

    @Test
    void addJobSchedule_shouldReturnCreated() throws Exception {
        JobScheduleCmd request = new JobScheduleCmd();
        request.setScheduleDate(LocalDate.of(2026, 6, 1));
        request.setStartTime(LocalTime.of(9, 0));
        request.setEndTime(LocalTime.of(18, 0));
        request.setSlotsAvailable(10);

        JobScheduleVO response = new JobScheduleVO();
        response.setId(1L);
        response.setJobId(100L);
        response.setSlotsAvailable(10);

        when(jobService.addJobSchedule(eq(100L), any(JobScheduleCmd.class))).thenReturn(response);

        request.setJobId(100L);

        mockMvc.perform(post("/api/jobs/schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.slotsAvailable").value(10));
    }

    @Test
    void removeJobSchedule_shouldReturnNoContent() throws Exception {
        mockMvc.perform(post("/api/jobs/schedules/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":1}"))
                .andExpect(status().isOk());

        verify(jobService).removeJobSchedule(1L);
    }
}
