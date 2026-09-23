package com.parttime.enterprise.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.parttime.enterprise.pojo.cmd.AnnotationBatchCreateCmd;
import com.parttime.enterprise.pojo.cmd.AnnotationBatchListCmd;
import com.parttime.enterprise.pojo.cmd.AnnotationBatchToggleCmd;
import com.parttime.enterprise.pojo.cmd.AnnotationBatchUpdateCmd;
import com.parttime.enterprise.pojo.vo.AnnotationBatchVO;
import com.parttime.enterprise.service.AnnotationBatchService;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AnnotationBatchControllerTest {

    @Mock
    private AnnotationBatchService annotationBatchService;

    @InjectMocks
    private AnnotationBatchController annotationBatchController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(annotationBatchController, "annotationBatchService", annotationBatchService);
        mockMvc = MockMvcBuilders.standaloneSetup(annotationBatchController).build();
    }

    @Test
    void list_shouldDelegateToServiceWithCompanyId() throws Exception {
        AnnotationBatchVO vo = new AnnotationBatchVO();
        vo.setId(5L);
        vo.setJobId(10L);
        vo.setBatchCode("B1");
        vo.setTotalItems(1000);
        vo.setStatus("ACTIVE");
        when(annotationBatchService.list(eq(1L), any(AnnotationBatchListCmd.class))).thenReturn(List.of(vo));

        String json = """
                {"jobId": 10}
                """;

        mockMvc.perform(post("/api/enterprise/annotation-batches/list")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].batchCode").value("B1"))
                .andExpect(jsonPath("$.data[0].totalItems").value(1000));

        verify(annotationBatchService).list(eq(1L), any(AnnotationBatchListCmd.class));
    }

    @Test
    void create_shouldDelegateToServiceWithCompanyId() throws Exception {
        AnnotationBatchVO vo = new AnnotationBatchVO();
        vo.setId(5L);
        vo.setBatchCode("B1");
        vo.setTotalItems(1000);
        vo.setStatus("ACTIVE");
        when(annotationBatchService.create(eq(1L), any(AnnotationBatchCreateCmd.class))).thenReturn(vo);

        String json = """
                {"jobId": 10, "batchCode": "B1", "totalItems": 1000, "externalBatchId": "EXT1"}
                """;

        mockMvc.perform(post("/api/enterprise/annotation-batches/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.batchCode").value("B1"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));

        verify(annotationBatchService).create(eq(1L), any(AnnotationBatchCreateCmd.class));
    }

    @Test
    void update_shouldDelegateToServiceWithCompanyId() throws Exception {
        AnnotationBatchVO vo = new AnnotationBatchVO();
        vo.setId(5L);
        vo.setBatchCode("B2");
        vo.setTotalItems(2000);
        when(annotationBatchService.update(eq(1L), any(AnnotationBatchUpdateCmd.class))).thenReturn(vo);

        String json = """
                {"id": 5, "batchCode": "B2", "totalItems": 2000, "externalBatchId": "EXT2"}
                """;

        mockMvc.perform(post("/api/enterprise/annotation-batches/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.batchCode").value("B2"))
                .andExpect(jsonPath("$.data.totalItems").value(2000));

        verify(annotationBatchService).update(eq(1L), any(AnnotationBatchUpdateCmd.class));
    }

    @Test
    void toggle_shouldDelegateToServiceWithCompanyId() throws Exception {
        AnnotationBatchVO vo = new AnnotationBatchVO();
        vo.setId(5L);
        vo.setStatus("CANCELLED");
        when(annotationBatchService.toggle(eq(1L), any(AnnotationBatchToggleCmd.class))).thenReturn(vo);

        String json = """
                {"id": 5, "status": "CANCELLED"}
                """;

        mockMvc.perform(post("/api/enterprise/annotation-batches/toggle")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));

        verify(annotationBatchService).toggle(eq(1L), any(AnnotationBatchToggleCmd.class));
    }
}
