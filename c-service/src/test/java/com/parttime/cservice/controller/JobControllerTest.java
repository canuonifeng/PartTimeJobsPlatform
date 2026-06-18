package com.parttime.cservice.controller;

import com.parttime.cservice.pojo.vo.JobScheduleInfoVO;
import com.parttime.cservice.pojo.cmd.ApplyJobCmd;
import com.parttime.cservice.pojo.entity.ScheduleApplication;
import com.parttime.cservice.pojo.vo.JobDetailVO;
import com.parttime.cservice.pojo.vo.JobSummaryVO;
import com.parttime.cservice.pojo.vo.JobRateInfoVO;
import com.parttime.cservice.pojo.vo.PageVO;
import com.parttime.cservice.pojo.vo.ProfileCompletenessVO;
import com.parttime.cservice.pojo.vo.WorkerSignupVO;
import com.parttime.cservice.service.ProfileService;
import com.parttime.cservice.service.impl.JobServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class JobControllerTest {

    private MockMvc mockMvc;
    @Mock
    private JobServiceImpl jobService;
    @Mock
    private ProfileService profileService;
    @InjectMocks
    private JobController controller;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        SecurityContextHolder.clearContext();
    }

    @Test
    void searchJobs_shouldReturn200WithJobList() throws Exception {
        JobSummaryVO job1 = new JobSummaryVO();
        job1.setId(1L);
        job1.setTitle("Software Engineer");
        job1.setLocation("Beijing");
        job1.setCategoryName("Technology");
        job1.setMinRate(new BigDecimal("50.00"));
        job1.setMaxRate(new BigDecimal("400.00"));
        job1.setRateTypes(List.of("HOURLY", "DAILY"));

        JobSummaryVO job2 = new JobSummaryVO();
        job2.setId(2L);
        job2.setTitle("Designer");
        job2.setLocation("Shanghai");
        job2.setCategoryName("Technology");
        job2.setMinRate(new BigDecimal("80.00"));
        job2.setMaxRate(new BigDecimal("80.00"));
        job2.setRateTypes(List.of("HOURLY"));

        when(jobService.searchJobs(null, null, null, null, null, null, null, 1, 10))
                .thenReturn(new PageVO<>(List.of(job1, job2), 2));

        mockMvc.perform(get("/api/worker/jobs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.records.length()").value(2))
                .andExpect(jsonPath("$.data.records[0].title").value("Software Engineer"))
                .andExpect(jsonPath("$.data.records[1].title").value("Designer"));
    }

    @Test
    void searchJobs_withQueryParams_shouldFilter() throws Exception {
        JobSummaryVO job = new JobSummaryVO();
        job.setId(1L);
        job.setTitle("Software Engineer");
        job.setLocation("Beijing");
        job.setCategoryName("Technology");
        job.setMinRate(new BigDecimal("50.00"));
        job.setMaxRate(new BigDecimal("400.00"));
        job.setRateTypes(List.of("HOURLY", "DAILY"));

        when(jobService.searchJobs(eq("engineer"), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), eq(1), eq(10)))
                .thenReturn(new PageVO<>(List.of(job), 1));

        mockMvc.perform(get("/api/worker/jobs?keyword=engineer"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records.length()").value(1))
                .andExpect(jsonPath("$.data.records[0].title").value("Software Engineer"));
    }

    @Test
    void getJobDetail_shouldReturn200WithJobDetail() throws Exception {
        JobRateInfoVO rate = new JobRateInfoVO();
        rate.setId(1L);
        rate.setType("HOURLY");
        rate.setAmount(new BigDecimal("50.00"));
        rate.setCurrency("CNY");

        JobScheduleInfoVO schedule = new JobScheduleInfoVO();
        schedule.setId(1L);
        schedule.setDate(LocalDate.of(2026, 6, 1));
        schedule.setStartTime("09:00");
        schedule.setEndTime("18:00");
        schedule.setSlotsAvailable(5);

        JobDetailVO detail = new JobDetailVO();
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

        when(jobService.getJobDetail(1L, null)).thenReturn(detail);

        mockMvc.perform(get("/api/worker/jobs/detail?id=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.title").value("Software Engineer"))
                .andExpect(jsonPath("$.data.description").value("负责后端系统开发与维护"))
                .andExpect(jsonPath("$.data.location").value("Beijing"))
                .andExpect(jsonPath("$.data.status").value("PUBLISHED"))
                .andExpect(jsonPath("$.data.rates[0].type").value("HOURLY"))
                .andExpect(jsonPath("$.data.schedules[0].date").value("2026-06-01"));
    }

    @Test
    void getJobDetail_withAuth_shouldIncludeApplyStatus() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("1", null, List.of()));

        JobDetailVO detail = new JobDetailVO();
        detail.setId(1L);
        detail.setTitle("Software Engineer");
        detail.setStatus("PUBLISHED");
        detail.setApplyStatus("已报名");

        when(jobService.getJobDetail(1L, 1L)).thenReturn(detail);

        mockMvc.perform(get("/api/worker/jobs/detail?id=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.applyStatus").value("已报名"));
    }

    @Test
    void applyForJob_withAuth_shouldReturn200() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("1", null, List.of()));

        ApplyJobCmd request = new ApplyJobCmd(1L, List.of(1L, 2L));

        when(profileService.getCompleteness(1L)).thenReturn(new ProfileCompletenessVO(true, List.of()));
        when(jobService.applyForJob(1L, 1L, List.of(1L, 2L))).thenReturn(true);

        mockMvc.perform(post("/api/worker/jobs/apply?id=1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.success").value(true));
    }

    @Test
    void applyForJob_withoutAuth_shouldReturn401() throws Exception {
        ApplyJobCmd request = new ApplyJobCmd(1L, List.of(1L));

        mockMvc.perform(post("/api/worker/jobs/apply?id=1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void getMySignups_withAuth_shouldReturnPagedApplications() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("1", null, List.of()));

        WorkerSignupVO signup = new WorkerSignupVO();
        signup.setApplicationId(10L);
        signup.setJobId(2L);
        signup.setScheduleId(3L);
        signup.setJobTitle("仓库分拣员");
        signup.setCompanyName("绿地物流");
        signup.setStatus("PENDING");
        signup.setWorkDate(LocalDate.of(2026, 6, 7));
        signup.setStartTime(LocalTime.of(9, 0));
        signup.setEndTime(LocalTime.of(18, 0));
        signup.setLocation("绿地物流园");
        signup.setPayAmount(new BigDecimal("180.00"));
        signup.setPayType("DAILY");
        signup.setAppliedAt(LocalDateTime.of(2026, 6, 6, 10, 0));

        when(jobService.getMySignups(1L, 1, 10)).thenReturn(new PageVO<>(List.of(signup), 1));

        mockMvc.perform(get("/api/worker/jobs/applications/my?page=1&pageSize=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records.length()").value(1))
                .andExpect(jsonPath("$.data.records[0].applicationId").value(10))
                .andExpect(jsonPath("$.data.records[0].jobTitle").value("仓库分拣员"))
                .andExpect(jsonPath("$.data.records[0].companyName").value("绿地物流"));
    }

    @Test
    void getMySignups_withoutAuth_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/worker/jobs/applications/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void getApplicationStatus_withAuth_shouldReturn200() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("1", null, List.of()));

        ScheduleApplication app = new ScheduleApplication();
        app.setId(10L);
        app.setScheduleId(1L);
        app.setWorkerId(1L);
        app.setStatus("PENDING");
        app.setAppliedAt(LocalDateTime.now());

        when(jobService.getApplicationStatus(1L, 1L)).thenReturn(List.of(app));

        mockMvc.perform(get("/api/worker/jobs/application?id=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(10))
                .andExpect(jsonPath("$.data[0].status").value("PENDING"));
    }

    @Test
    void getApplicationStatus_withoutAuth_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/worker/jobs/application?id=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401));
    }
}
