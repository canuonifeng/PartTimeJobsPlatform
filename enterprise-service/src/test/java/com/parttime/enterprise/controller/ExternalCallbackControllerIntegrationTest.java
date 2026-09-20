package com.parttime.enterprise.controller;

import com.parttime.enterprise.service.ExternalCallbackService;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.test.util.ReflectionTestUtils;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 17.2 外部系统联调测试：模拟外部标注系统通过 HTTP 调用企业端回调接口，
 * 验证请求绑定、鉴权头传递与响应契约。
 */
class ExternalCallbackControllerIntegrationTest {

    private MockMvc mockMvc;
    private ExternalCallbackService externalCallbackService;

    @BeforeEach
    void setUp() {
        externalCallbackService = mock(ExternalCallbackService.class);
        ExternalCallbackController controller = new ExternalCallbackController();
        ReflectionTestUtils.setField(controller, "externalCallbackService", externalCallbackService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .build();
    }

    @Test
    void submitCallback_shouldAcceptExternalSubmission() throws Exception {
        mockMvc.perform(post("/api/enterprise/external/annotation/submit")
                        .header("X-Callback-Key", "secret-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"externalTaskId\":\"EXT_TASK_001\",\"externalBatchId\":\"BATCH_001\",\"externalWorkerId\":\"EXT_WORKER_1\",\"externalSubmissionId\":\"SUB_001\",\"itemsCompleted\":80}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void qualityCheckCallback_shouldAcceptQualityResult() throws Exception {
        mockMvc.perform(post("/api/enterprise/external/annotation/quality-check")
                        .header("X-Callback-Key", "secret-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"externalTaskId\":\"EXT_TASK_001\",\"externalBatchId\":\"BATCH_001\",\"externalWorkerId\":\"EXT_WORKER_1\",\"passed\":true,\"itemsCompleted\":80}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void progressCallback_shouldAcceptProgressUpdate() throws Exception {
        mockMvc.perform(post("/api/enterprise/external/annotation/progress")
                        .header("X-Callback-Key", "secret-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"externalTaskId\":\"EXT_TASK_001\",\"externalBatchId\":\"BATCH_001\",\"externalWorkerId\":\"EXT_WORKER_1\",\"itemsCompleted\":30}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
