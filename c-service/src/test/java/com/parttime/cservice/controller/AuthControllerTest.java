package com.parttime.cservice.controller;

import com.parttime.cservice.config.JwtTokenProvider;
import com.parttime.cservice.pojo.vo.LoginVO;
import com.parttime.cservice.pojo.cmd.RegisterCmd;
import com.parttime.cservice.pojo.vo.WorkerVO;
import com.parttime.cservice.service.impl.WorkerServiceImpl;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerTest {

    private MockMvc mockMvc;
    @Mock
    private WorkerServiceImpl workerService;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @InjectMocks
    private AuthController controller;
    private ObjectMapper objectMapper;

    private WorkerVO sampleWorker;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        sampleWorker = new WorkerVO();
        sampleWorker.setId(1L);
        sampleWorker.setName("John");
        sampleWorker.setPhone("13800138000");
        sampleWorker.setAvatarUrl("http://avatar.url");
        sampleWorker.setCreatedAt(LocalDateTime.now());

        SecurityContextHolder.clearContext();
    }

    @Test
    void register_shouldReturn200WithToken() throws Exception {
        RegisterCmd request = new RegisterCmd("John", "13800138000", "http://avatar.url", null);

        when(workerService.register(any(RegisterCmd.class))).thenReturn(sampleWorker);
        when(jwtTokenProvider.generateToken(eq("1"), any())).thenReturn("test.jwt.token");

        mockMvc.perform(post("/api/worker/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").value("test.jwt.token"))
                .andExpect(jsonPath("$.data.workerId").value(1));
    }

    @Test
    void login_shouldReturn200WithToken() throws Exception {
        when(workerService.login("wx_test_code")).thenReturn("test.jwt.token");
        when(jwtTokenProvider.getUserIdFromToken("test.jwt.token")).thenReturn("1");

        mockMvc.perform(post("/api/worker/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"wechatCode\":\"wx_test_code\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").value("test.jwt.token"))
                .andExpect(jsonPath("$.data.workerId").value(1));
    }

    @Test
    void getProfile_shouldReturnWorkerInfo() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("1", null, List.of()));

        when(workerService.getWorkerById(1L)).thenReturn(sampleWorker);

        mockMvc.perform(get("/api/worker/auth/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("John"))
                .andExpect(jsonPath("$.data.phone").value("13800138000"));
    }

    @Test
    void getProfile_shouldReturnUnauthorizedWhenNotAuthenticated() throws Exception {
        SecurityContextHolder.clearContext();

        mockMvc.perform(get("/api/worker/auth/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void wechatLogin_shouldReturn200WithToken() throws Exception {
        LoginVO wechatResponse = new LoginVO("wechat.jwt.token", 1L, "openid_123", "nickname");

        when(workerService.loginWithWechat("test_code", null)).thenReturn(wechatResponse);

        mockMvc.perform(post("/api/worker/auth/wechat-login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"test_code\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").value("wechat.jwt.token"))
                .andExpect(jsonPath("$.data.workerId").value(1))
                .andExpect(jsonPath("$.data.openId").value("openid_123"))
                .andExpect(jsonPath("$.data.nickname").value("nickname"));
    }

    @Test
    void updateProfile_shouldModifyFields() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("1", null, List.of()));

        WorkerVO updatedWorker = new WorkerVO();
        updatedWorker.setId(1L);
        updatedWorker.setName("John Updated");
        updatedWorker.setPhone("13900139000");
        updatedWorker.setAvatarUrl("http://new.avatar");
        updatedWorker.setCreatedAt(LocalDateTime.now());

        RegisterCmd updateRequest = new RegisterCmd("John Updated", "13900139000", "http://new.avatar", null);

        when(workerService.updateProfile(eq(1L), any(RegisterCmd.class))).thenReturn(updatedWorker);

        mockMvc.perform(post("/api/worker/auth/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").value("John Updated"))
                .andExpect(jsonPath("$.data.phone").value("13900139000"))
                .andExpect(jsonPath("$.data.avatarUrl").value("http://new.avatar"));
    }
}
