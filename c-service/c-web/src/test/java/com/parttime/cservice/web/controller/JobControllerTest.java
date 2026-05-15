package com.parttime.cservice.web.controller;

import com.parttime.cservice.core.dto.*;
import com.parttime.cservice.core.service.JobService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class JobControllerTest {

    private MockMvc mockMvc;
    private JobService jobService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        jobService = mock(JobService.class);
        objectMapper = new ObjectMapper();

        JobController controller = new JobController(jobService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        SecurityContextHolder.clearContext();
    }

    @Test
    void searchJobs_shouldReturn200WithJobList() throws Exception {
        JobSummary job1 = new JobSummary();
        job1.setId(1L);
        job1.setTitle("Software Engineer");
        job1.setLocation("Beijing");
        job1.setCategoryName("Technology");
        job1.setMinRate(new BigDecimal("50.00"));
        job1.setMaxRate(new BigDecimal("400.00"));
        job1.setRateTypes(List.of("HOURLY", "DAILY"));

        JobSummary job2 = new JobSummary();
        job2.setId(2L);
        job2.setTitle("Designer");
        job2.setLocation("Shanghai");
        job2.setCategoryName("Technology");
        job2.setMinRate(new BigDecimal("80.00"));
        job2.setMaxRate(new BigDecimal("80.00"));
        job2.setRateTypes(List.of("HOURLY"));

        when(jobService.searchJobs(null, null, null, null, null))
                .thenReturn(List.of(job1, job2));

        mockMvc.perform(get("/api/jobs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("Software Engineer"))
                .andExpect(jsonPath("$[1].title").value("Designer"));
    }

    @Test
    void searchJobs_withQueryParams_shouldFilter() throws Exception {
        JobSummary job = new JobSummary();
        job.setId(1L);
        job.setTitle("Software Engineer");
        job.setLocation("Beijing");
        job.setCategoryName("Technology");
        job.setMinRate(new BigDecimal("50.00"));
        job.setMaxRate(new BigDecimal("400.00"));
        job.setRateTypes(List.of("HOURLY", "DAILY"));

        when(jobService.searchJobs(eq("engineer"), isNull(), isNull(), isNull(), isNull()))
                .thenReturn(List.of(job));

        mockMvc.perform(get("/api/jobs?keyword=engineer"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Software Engineer"));
    }

    @Test
    void getJobDetail_shouldReturn200WithJobDetail() throws Exception {
        JobRateInfo rate = new JobRateInfo();
        rate.setId(1L);
        rate.setType("HOURLY");
        rate.setAmount(new BigDecimal("50.00"));
        rate.setCurrency("CNY");

        JobScheduleInfo schedule = new JobScheduleInfo();
        schedule.setId(1L);
        schedule.setDate(LocalDate.of(2026, 6, 1));
        schedule.setStartTime("09:00");
        schedule.setEndTime("18:00");
        schedule.setSlotsAvailable(5);

        JobDetail detail = new JobDetail();
        detail.setId(1L);
        detail.setTitle("Software Engineer");
        detail.setDescription("负责后端系统开发与维护");
        detail.setLocation("Beijing");
        detail.setCategoryName("Technology");
        detail.setRates(List.of(rate));
        detail.setSchedules(List.of(schedule));
        detail.setHeadcount(10);
        detail.setAcceptedCount(3);
        detail.setDeadline(LocalDateTime.of(2026, 6, 30, 23, 59));
        detail.setStatus("PUBLISHED");

        when(jobService.getJobDetail(1L)).thenReturn(detail);

        mockMvc.perform(get("/api/jobs/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Software Engineer"))
                .andExpect(jsonPath("$.description").value("负责后端系统开发与维护"))
                .andExpect(jsonPath("$.location").value("Beijing"))
                .andExpect(jsonPath("$.status").value("PUBLISHED"))
                .andExpect(jsonPath("$.rates[0].type").value("HOURLY"))
                .andExpect(jsonPath("$.schedules[0].date").value("2026-06-01"));
    }

    @Test
    void applyForJob_withAuth_shouldReturn200() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("1", null, List.of()));

        ApplicationRequest request = new ApplicationRequest(1L, List.of(1L, 2L));

        when(jobService.applyForJob(1L, 1L, List.of(1L, 2L))).thenReturn(true);

        mockMvc.perform(post("/api/jobs/1/apply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void applyForJob_withoutAuth_shouldReturn401() throws Exception {
        ApplicationRequest request = new ApplicationRequest(1L, List.of(1L));

        mockMvc.perform(post("/api/jobs/1/apply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getApplicationStatus_withAuth_shouldReturn200() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("1", null, List.of()));

        ApplicationResponse app = new ApplicationResponse();
        app.setApplicationId(10L);
        app.setJobId(1L);
        app.setStatus("PENDING");
        app.setAppliedAt(LocalDateTime.now());

        when(jobService.getApplicationStatus(1L, 1L)).thenReturn(List.of(app));

        mockMvc.perform(get("/api/jobs/1/application"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].applicationId").value(10))
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    void getApplicationStatus_withoutAuth_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/jobs/1/application"))
                .andExpect(status().isUnauthorized());
    }
}
