package com.parttime.cservice.controller;

import com.parttime.cservice.pojo.vo.EarningsSummaryVO;
import com.parttime.cservice.pojo.vo.HomeStatsVO;
import com.parttime.cservice.pojo.vo.ProfileVO;
import com.parttime.cservice.pojo.cmd.ProfileUpdateCmd;
import com.parttime.cservice.pojo.vo.ResumeVO;
import com.parttime.cservice.pojo.vo.WorkerRealNameAuthVO;
import com.parttime.cservice.pojo.cmd.ResumeUploadCmd;
import com.parttime.cservice.service.HomeService;
import com.parttime.cservice.service.WithdrawalService;
import com.parttime.cservice.service.WorkerRealNameAuthService;
import com.parttime.cservice.service.impl.ProfileServiceImpl;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ProfileControllerTest {

    private MockMvc mockMvc;
    @Mock
    private ProfileServiceImpl profileService;
    @Mock
    private HomeService homeService;
    @Mock
    private WithdrawalService withdrawalService;
    @Mock
    private WorkerRealNameAuthService workerRealNameAuthService;
    @InjectMocks
    private ProfileController controller;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void getProfile_shouldReturnProfile() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("1", null, List.of()));

        ProfileVO response = new ProfileVO();
        response.setWorkerId(1L);
        response.setName("John");

        when(profileService.getProfile(1L)).thenReturn(response);

        mockMvc.perform(get("/api/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("John"));
    }

    @Test
    void getProfile_shouldReturnUnauthorizedWhenNotAuthenticated() throws Exception {
        SecurityContextHolder.clearContext();
        mockMvc.perform(get("/api/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void updateProfile_shouldReturnUpdatedProfile() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("1", null, List.of()));

        ProfileVO response = new ProfileVO();
        response.setWorkerId(1L);
        response.setName("John Updated");

        when(profileService.updateProfile(eq(1L), any())).thenReturn(response);

        ProfileUpdateCmd request = new ProfileUpdateCmd();
        request.setName("John Updated");
        request.setPhone("13900139000");

        mockMvc.perform(put("/api/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("John Updated"));
    }

    @Test
    void uploadResume_shouldReturnCreated() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("1", null, List.of()));

        ResumeVO response = new ResumeVO();
        response.setId(1L);
        response.setFileName("resume.pdf");

        when(profileService.uploadResume(eq(1L), eq("resume.pdf"), eq("http://files/resume.pdf")))
                .thenReturn(response);

        ResumeUploadCmd request = new ResumeUploadCmd();
        request.setFileName("resume.pdf");
        request.setFileUrl("http://files/resume.pdf");

        mockMvc.perform(post("/api/profile/resumes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fileName").value("resume.pdf"));
    }

    @Test
    void getResumes_shouldReturnList() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("1", null, List.of()));

        ResumeVO r1 = new ResumeVO();
        r1.setId(1L);
        r1.setFileName("resume.pdf");

        when(profileService.getResumes(1L)).thenReturn(List.of(r1));

        mockMvc.perform(get("/api/profile/resumes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    void getDashboard_shouldReturnProfileStatsAndEarnings() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("1", null, List.of()));

        ProfileVO profile = new ProfileVO();
        profile.setWorkerId(1L);
        profile.setName("John");

        HomeStatsVO stats = new HomeStatsVO();
        stats.setMonthHours(new BigDecimal("12.50"));
        stats.setMonthIncome(new BigDecimal("680.00"));
        stats.setAttendanceDays(3);

        EarningsSummaryVO earnings = new EarningsSummaryVO();
        earnings.setTotalEarned(new BigDecimal("4680.00"));
        earnings.setTotalWithdrawn(new BigDecimal("4000.00"));
        earnings.setPendingWithdrawal(new BigDecimal("680.00"));

        WorkerRealNameAuthVO realNameAuth = new WorkerRealNameAuthVO();
        realNameAuth.setStatus("APPROVED");
        realNameAuth.setRealName("John");

        when(profileService.getProfile(1L)).thenReturn(profile);
        when(homeService.getStats(1L)).thenReturn(stats);
        when(withdrawalService.getEarningsSummary(1L)).thenReturn(earnings);
        when(workerRealNameAuthService.getStatus(1L)).thenReturn(realNameAuth);

        mockMvc.perform(get("/api/profile/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.profile.name").value("John"))
                .andExpect(jsonPath("$.data.stats.monthHours").value(12.50))
                .andExpect(jsonPath("$.data.stats.attendanceDays").value(3))
                .andExpect(jsonPath("$.data.earningsSummary.totalEarned").value(4680.00))
                .andExpect(jsonPath("$.data.realNameAuth.status").value("APPROVED"));
    }

    @Test
    void getDashboard_shouldReturnUnauthorizedWhenNotAuthenticated() throws Exception {
        SecurityContextHolder.clearContext();
        mockMvc.perform(get("/api/profile/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void getProfile_shouldReturnBadRequestOnError() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("1", null, List.of()));

        when(profileService.getProfile(1L)).thenThrow(new RuntimeException("Profile not found"));

        mockMvc.perform(get("/api/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));
    }
}
