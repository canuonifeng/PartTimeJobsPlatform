package com.parttime.enterprise.controller;

import com.parttime.enterprise.pojo.vo.JobShareCodeVO;
import com.parttime.enterprise.pojo.vo.JobShareLinkVO;
import com.parttime.enterprise.service.JobShareService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class JobShareControllerTest {

    private MockMvc mockMvc;

    @Mock
    private JobShareService jobShareService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new JobShareController(jobShareService)).build();
    }

    @Test
    void getShareCode_returns_jobId_path_and_base64_image() throws Exception {
        when(jobShareService.getShareCode(42L))
                .thenReturn(new JobShareCodeVO(42L, "/pages/jobs/jobDetail?scene=42", "iVBORw0KGgoAAA"));

        mockMvc.perform(get("/api/enterprise/jobs/share-code").param("id", "42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.jobId").value(42))
                .andExpect(jsonPath("$.data.path").value("/pages/jobs/jobDetail?scene=42"))
                .andExpect(jsonPath("$.data.imageBase64").value("iVBORw0KGgoAAA"));
    }

    @Test
    void getShareLink_returns_jobId_and_scheme_link() throws Exception {
        when(jobShareService.getShareLink(42L))
                .thenReturn(new JobShareLinkVO(42L, "weixin://dl/business/?appid=test_appid&path=/pages/jobs/jobDetail&query=id%3D42&env_version=release"));

        mockMvc.perform(get("/api/enterprise/jobs/share-link").param("id", "42")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.jobId").value(42))
                .andExpect(jsonPath("$.data.link").value("weixin://dl/business/?appid=test_appid&path=/pages/jobs/jobDetail&query=id%3D42&env_version=release"));
    }
}
