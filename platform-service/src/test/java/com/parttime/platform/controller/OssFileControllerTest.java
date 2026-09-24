package com.parttime.platform.controller;

import com.parttime.platform.pojo.vo.OssStsVO;
import com.parttime.platform.pojo.vo.SignedUrlVO;
import com.parttime.platform.service.OssSignedUrlService;
import com.parttime.platform.service.OssStsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OssFileControllerTest {

    @Mock
    private OssStsService ossStsService;
    @Mock
    private OssSignedUrlService ossSignedUrlService;
    @InjectMocks
    private FileStsController stsController;
    @InjectMocks
    private FileSignedUrlController signedUrlController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(stsController, signedUrlController).build();
        SecurityContextHolder.clearContext();
    }

    private void loginAdmin(String adminName) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(adminName, null, List.of()));
    }

    @Test
    void sts_unauthenticated_returns401() throws Exception {
        mockMvc.perform(post("/api/admin/files/sts").param("biz", "review"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void signedUrl_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/admin/files/signed-url").param("key", "worker/1/a.jpg"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void sts_illegalArgument_returns400() throws Exception {
        loginAdmin("admin");
        when(ossStsService.issueSts(eq("admin"), eq("bogus")))
                .thenThrow(new IllegalArgumentException("unsupported biz"));

        mockMvc.perform(post("/api/admin/files/sts").param("biz", "bogus"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("unsupported biz"));
    }

    @Test
    void sts_illegalState_returns503() throws Exception {
        loginAdmin("admin");
        when(ossStsService.issueSts(eq("admin"), eq("review")))
                .thenThrow(new IllegalStateException("oss not configured"));

        mockMvc.perform(post("/api/admin/files/sts").param("biz", "review"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(503));
    }

    @Test
    void sts_success_returns200AndData() throws Exception {
        loginAdmin("admin");
        OssStsVO vo = new OssStsVO();
        vo.setBucket("test-bucket");
        vo.setEndpoint("https://oss.example.com");
        vo.setAccessKeyId("ak-id");
        vo.setAccessKeySecret("ak-secret");
        vo.setSecurityToken("token");
        vo.setExpiration("2026-09-24T12:00:00Z");
        vo.setPrefix("admin/");
        when(ossStsService.issueSts(eq("admin"), eq("review"))).thenReturn(vo);

        mockMvc.perform(post("/api/admin/files/sts").param("biz", "review"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.bucket").value("test-bucket"))
                .andExpect(jsonPath("$.data.prefix").value("admin/"));
    }

    @Test
    void signedUrl_illegalArgument_returns404() throws Exception {
        loginAdmin("admin");
        when(ossSignedUrlService.getSignedUrl(eq("admin"), eq("bad-key")))
                .thenThrow(new IllegalArgumentException("key not found"));

        mockMvc.perform(get("/api/admin/files/signed-url").param("key", "bad-key"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("key not found"));
    }

    @Test
    void signedUrl_security_returns403() throws Exception {
        loginAdmin("admin");
        when(ossSignedUrlService.getSignedUrl(eq("admin"), eq("private/secret.bin")))
                .thenThrow(new SecurityException("prefix not allowed for admin"));

        mockMvc.perform(get("/api/admin/files/signed-url").param("key", "private/secret.bin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    void signedUrl_illegalState_returns503() throws Exception {
        loginAdmin("admin");
        when(ossSignedUrlService.getSignedUrl(eq("admin"), eq("worker/1/a.jpg")))
                .thenThrow(new IllegalStateException("oss not configured"));

        mockMvc.perform(get("/api/admin/files/signed-url").param("key", "worker/1/a.jpg"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(503));
    }

    @Test
    void signedUrl_success_returns200AndData() throws Exception {
        loginAdmin("admin");
        SignedUrlVO vo = new SignedUrlVO();
        vo.setUrl("https://oss.example.com/worker/1/a.jpg?signed=1");
        vo.setExpiresAt(1727100000000L);
        when(ossSignedUrlService.getSignedUrl(eq("admin"), eq("worker/1/a.jpg"))).thenReturn(vo);

        mockMvc.perform(get("/api/admin/files/signed-url").param("key", "worker/1/a.jpg"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.url").value("https://oss.example.com/worker/1/a.jpg?signed=1"))
                .andExpect(jsonPath("$.data.expiresAt").value(1727100000000L));
    }
}
