package com.parttime.enterprise.controller;

import com.parttime.enterprise.pojo.vo.EvaluationVO;
import com.parttime.enterprise.pojo.vo.WorkHistoryVO;
import com.parttime.enterprise.pojo.vo.WorkerProfileVO;
import com.parttime.enterprise.service.WorkerProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class WorkerProfileControllerTest {

    @Mock
    private WorkerProfileService workerProfileService;

    @InjectMocks
    private WorkerProfileController workerProfileController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(workerProfileController, "workerProfileService", workerProfileService);
        mockMvc = MockMvcBuilders.standaloneSetup(workerProfileController).build();
    }

    @Test
    void getProfile_shouldReturnProfile() throws Exception {
        WorkerProfileVO response = new WorkerProfileVO();
        response.setWorkerId(10L);
        response.setAvgRating(4.5);
        response.setIsBlacklisted(false);
        response.setTotalEvaluations(1);

        when(workerProfileService.getWorkerProfile(1L, 10L)).thenReturn(response);

        mockMvc.perform(get("/api/workers/profile")
                        .param("workerId", "10")
                        .param("companyId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.workerId").value(10))
                .andExpect(jsonPath("$.avgRating").value(4.5))
                .andExpect(jsonPath("$.isBlacklisted").value(false));
    }

    @Test
    void evaluateWorker_shouldReturnCreated() throws Exception {
        EvaluationVO response = new EvaluationVO();
        response.setId(99L);
        response.setWorkerId(10L);
        response.setRating(5);

        when(workerProfileService.evaluateWorker(eq(1L), eq(50L), eq(10L), eq(5), eq("Great"))).thenReturn(response);

        String json = """
                {
                    "companyId": 1,
                    "jobId": 50,
                    "rating": 5,
                    "comment": "Great"
                }
                """;

        mockMvc.perform(post("/api/workers/evaluations").param("workerId", "10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(99))
                .andExpect(jsonPath("$.rating").value(5));
    }

    @Test
    void getEvaluations_shouldReturnList() throws Exception {
        EvaluationVO e1 = new EvaluationVO();
        e1.setId(1L);
        e1.setRating(4);

        when(workerProfileService.getEvaluations(10L, 1L)).thenReturn(List.of(e1));

        mockMvc.perform(get("/api/workers/evaluations").param("workerId", "10")
                        .param("companyId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].rating").value(4));
    }

    @Test
    void addToBlacklist_shouldReturnCreated() throws Exception {
        String json = """
                {
                    "companyId": 1,
                    "reason": "No-show"
                }
                """;

        mockMvc.perform(post("/api/workers/blacklist").param("workerId", "10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated());

        verify(workerProfileService).addToBlacklist(1L, 10L, "No-show");
    }

    @Test
    void removeFromBlacklist_shouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/workers/blacklist")
                        .param("workerId", "10")
                        .param("companyId", "1"))
                .andExpect(status().isNoContent());

        verify(workerProfileService).removeFromBlacklist(1L, 10L);
    }

    @Test
    void getWorkHistory_shouldReturnList() throws Exception {
        WorkHistoryVO wh = new WorkHistoryVO();
        wh.setShiftId(100L);
        wh.setJobId(50L);
        wh.setShiftDate(LocalDate.of(2026, 5, 1));
        wh.setStartTime(LocalTime.of(9, 0));
        wh.setEndTime(LocalTime.of(18, 0));

        when(workerProfileService.getWorkHistory(10L, 1L)).thenReturn(List.of(wh));

        mockMvc.perform(get("/api/workers/work-history")
                        .param("workerId", "10")
                        .param("companyId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].shiftId").value(100));
    }

    @Test
    void addToBlacklist_shouldIgnoreOptionalReason() throws Exception {
        String json = """
                {
                    "companyId": 1
                }
                """;

        mockMvc.perform(post("/api/workers/blacklist").param("workerId", "10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated());

        verify(workerProfileService).addToBlacklist(1L, 10L, null);
    }

    @Test
    void evaluateWorker_shouldAcceptMinimalRequest() throws Exception {
        EvaluationVO response = new EvaluationVO();
        response.setId(1L);

        when(workerProfileService.evaluateWorker(any(), any(), any(), any(), isNull())).thenReturn(response);

        String json = """
                {
                    "companyId": 1,
                    "jobId": 50,
                    "rating": 3
                }
                """;

        mockMvc.perform(post("/api/workers/evaluations").param("workerId", "10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated());
    }
}
