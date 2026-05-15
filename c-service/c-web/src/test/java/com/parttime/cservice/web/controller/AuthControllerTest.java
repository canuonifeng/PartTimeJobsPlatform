package com.parttime.cservice.web.controller;

import com.parttime.cservice.core.auth.JwtTokenProvider;
import com.parttime.cservice.core.dto.WeChatLoginResponse;
import com.parttime.cservice.core.dto.WorkerRegisterRequest;
import com.parttime.cservice.core.dto.WorkerResponse;
import com.parttime.cservice.core.service.WorkerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerTest {

    private MockMvc mockMvc;
    private WorkerService workerService;
    private JwtTokenProvider jwtTokenProvider;
    private ObjectMapper objectMapper;

    private WorkerResponse sampleWorker;

    @BeforeEach
    void setUp() {
        workerService = mock(WorkerService.class);
        jwtTokenProvider = mock(JwtTokenProvider.class);
        objectMapper = new ObjectMapper();

        AuthController controller = new AuthController(workerService, jwtTokenProvider);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        sampleWorker = new WorkerResponse();
        sampleWorker.setId(1L);
        sampleWorker.setName("John");
        sampleWorker.setPhone("13800138000");
        sampleWorker.setAvatar("http://avatar.url");
        sampleWorker.setCreatedAt(LocalDateTime.now());

        SecurityContextHolder.clearContext();
    }

    @Test
    void register_shouldReturn200WithToken() throws Exception {
        WorkerRegisterRequest request = new WorkerRegisterRequest("John", "13800138000", "http://avatar.url");

        when(workerService.register(any(WorkerRegisterRequest.class))).thenReturn(sampleWorker);
        when(jwtTokenProvider.generateToken(eq("1"), any())).thenReturn("test.jwt.token");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("test.jwt.token"))
                .andExpect(jsonPath("$.workerId").value(1));
    }

    @Test
    void login_shouldReturn200WithToken() throws Exception {
        when(workerService.login("wx_test_code")).thenReturn("test.jwt.token");
        when(jwtTokenProvider.getUserIdFromToken("test.jwt.token")).thenReturn("1");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"wechatCode\":\"wx_test_code\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("test.jwt.token"))
                .andExpect(jsonPath("$.workerId").value(1));
    }

    @Test
    void getProfile_shouldReturnWorkerInfo() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("1", null, List.of()));

        when(workerService.getWorkerById(1L)).thenReturn(sampleWorker);

        mockMvc.perform(get("/api/auth/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("John"))
                .andExpect(jsonPath("$.phone").value("13800138000"));
    }

    @Test
    void wechatLogin_shouldReturn200WithToken() throws Exception {
        WeChatLoginResponse wechatResponse = new WeChatLoginResponse("wechat.jwt.token", 1L, "openid_123", "nickname");

        when(workerService.loginWithWechat("test_code")).thenReturn(wechatResponse);

        mockMvc.perform(post("/api/auth/wechat-login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"test_code\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("wechat.jwt.token"))
                .andExpect(jsonPath("$.workerId").value(1))
                .andExpect(jsonPath("$.openId").value("openid_123"))
                .andExpect(jsonPath("$.nickname").value("nickname"));
    }

    @Test
    void updateProfile_shouldModifyFields() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("1", null, List.of()));

        WorkerResponse updatedWorker = new WorkerResponse();
        updatedWorker.setId(1L);
        updatedWorker.setName("John Updated");
        updatedWorker.setPhone("13900139000");
        updatedWorker.setAvatar("http://new.avatar");
        updatedWorker.setCreatedAt(LocalDateTime.now());

        WorkerRegisterRequest updateRequest = new WorkerRegisterRequest("John Updated", "13900139000", "http://new.avatar");

        when(workerService.updateProfile(eq(1L), any(WorkerRegisterRequest.class))).thenReturn(updatedWorker);

        mockMvc.perform(put("/api/auth/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John Updated"))
                .andExpect(jsonPath("$.phone").value("13900139000"))
                .andExpect(jsonPath("$.avatar").value("http://new.avatar"));
    }
}
