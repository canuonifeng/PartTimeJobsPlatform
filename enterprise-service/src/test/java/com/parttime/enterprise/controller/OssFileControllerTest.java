package com.parttime.enterprise.controller;

import com.parttime.enterprise.pojo.vo.OssStsVO;
import com.parttime.enterprise.pojo.vo.SignedUrlVO;
import com.parttime.enterprise.service.OssSignedUrlService;
import com.parttime.enterprise.service.OssStsService;
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
import java.util.Map;

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

    private void loginEnterprise(Long companyId) {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken("ent-user", null, List.of());
        auth.setDetails(companyId == null ? Map.of() : Map.of("companyId", companyId));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void sts_unauthenticated_returns401() throws Exception {
        mockMvc.perform(post("/api/enterprise/files/sts").param("biz", "job"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void signedUrl_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/enterprise/files/signed-url").param("key", "enterprise/42/job.png"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void sts_authenticatedButMissingCompanyId_returns401() throws Exception {
        loginEnterprise(null);
        mockMvc.perform(post("/api/enterprise/files/sts").param("biz", "job"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void signedUrl_authenticatedButMissingCompanyId_returns401() throws Exception {
        loginEnterprise(null);
        mockMvc.perform(get("/api/enterprise/files/signed-url").param("key", "enterprise/42/job.png"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void sts_illegalArgument_returns400() throws Exception {
        loginEnterprise(42L);
        when(ossStsService.issueSts(eq(42L), eq("bogus")))
                .thenThrow(new IllegalArgumentException("unsupported biz"));

        mockMvc.perform(post("/api/enterprise/files/sts").param("biz", "bogus"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("unsupported biz"));
    }

    @Test
    void sts_illegalState_returns503() throws Exception {
        loginEnterprise(42L);
        when(ossStsService.issueSts(eq(42L), eq("job")))
                .thenThrow(new IllegalStateException("oss not configured"));

        mockMvc.perform(post("/api/enterprise/files/sts").param("biz", "job"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(503));
    }

    @Test
    void sts_success_returns200AndData() throws Exception {
        loginEnterprise(42L);
        OssStsVO vo = new OssStsVO();
        vo.setBucket("test-bucket");
        vo.setEndpoint("https://oss.example.com");
        vo.setAccessKeyId("ak-id");
        vo.setAccessKeySecret("ak-secret");
        vo.setSecurityToken("token");
        vo.setExpiration("2026-09-24T12:00:00Z");
        vo.setPrefix("enterprise/42/");
        when(ossStsService.issueSts(eq(42L), eq("job"))).thenReturn(vo);

        mockMvc.perform(post("/api/enterprise/files/sts").param("biz", "job"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.bucket").value("test-bucket"))
                .andExpect(jsonPath("$.data.prefix").value("enterprise/42/"));
    }

    @Test
    void signedUrl_illegalArgument_returns404() throws Exception {
        loginEnterprise(42L);
        when(ossSignedUrlService.getSignedUrl(eq(42L), eq("bad-key")))
                .thenThrow(new IllegalArgumentException("key not found"));

        mockMvc.perform(get("/api/enterprise/files/signed-url").param("key", "bad-key"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("key not found"));
    }

    @Test
    void signedUrl_security_returns403() throws Exception {
        loginEnterprise(42L);
        when(ossSignedUrlService.getSignedUrl(eq(42L), eq("enterprise/99/job.png")))
                .thenThrow(new SecurityException("cross enterprise access denied"));

        mockMvc.perform(get("/api/enterprise/files/signed-url").param("key", "enterprise/99/job.png"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    void signedUrl_illegalState_returns503() throws Exception {
        loginEnterprise(42L);
        when(ossSignedUrlService.getSignedUrl(eq(42L), eq("enterprise/42/job.png")))
                .thenThrow(new IllegalStateException("oss not configured"));

        mockMvc.perform(get("/api/enterprise/files/signed-url").param("key", "enterprise/42/job.png"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(503));
    }

    @Test
    void signedUrl_success_returns200AndData() throws Exception {
        loginEnterprise(42L);
        SignedUrlVO vo = new SignedUrlVO();
        vo.setUrl("https://oss.example.com/enterprise/42/job.png?signed=1");
        vo.setExpiresAt(1727100000000L);
        when(ossSignedUrlService.getSignedUrl(eq(42L), eq("enterprise/42/job.png"))).thenReturn(vo);

        mockMvc.perform(get("/api/enterprise/files/signed-url").param("key", "enterprise/42/job.png"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.url").value("https://oss.example.com/enterprise/42/job.png?signed=1"))
                .andExpect(jsonPath("$.data.expiresAt").value(1727100000000L));
    }
}
