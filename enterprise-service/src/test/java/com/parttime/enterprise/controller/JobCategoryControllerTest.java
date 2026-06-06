package com.parttime.enterprise.controller;

import com.parttime.enterprise.pojo.cmd.JobCategoryCmd;
import com.parttime.enterprise.pojo.vo.JobCategoryVO;
import com.parttime.enterprise.service.JobCategoryService;
import com.fasterxml.jackson.databind.ObjectMapper;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class JobCategoryControllerTest {

    @Mock
    private JobCategoryService jobCategoryService;

    @InjectMocks
    private JobCategoryController jobCategoryController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jobCategoryController, "jobCategoryService", jobCategoryService);
        mockMvc = MockMvcBuilders.standaloneSetup(jobCategoryController).build();
    }

    @Test
    void getAllCategories_shouldReturnCategories() throws Exception {
        JobCategoryVO cat = new JobCategoryVO();
        cat.setId(1L);
        cat.setName("Parent");

        when(jobCategoryService.getAllCategories()).thenReturn(List.of(cat));

        mockMvc.perform(get("/api/job-categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Parent"));

        verify(jobCategoryService).getAllCategories();
    }

    @Test
    void createCategory_shouldReturnCreated() throws Exception {
        JobCategoryCmd request = new JobCategoryCmd();
        request.setName("New Cat");
        request.setSortOrder(1);

        JobCategoryVO response = new JobCategoryVO();
        response.setId(1L);
        response.setName("New Cat");
        response.setSortOrder(1);

        when(jobCategoryService.createCategory(any(JobCategoryCmd.class))).thenReturn(response);

        mockMvc.perform(post("/api/job-categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("New Cat"));
    }

    @Test
    void updateCategory_shouldReturnOk() throws Exception {
        JobCategoryCmd request = new JobCategoryCmd();
        request.setName("Updated Cat");

        JobCategoryVO response = new JobCategoryVO();
        response.setId(1L);
        response.setName("Updated Cat");

        when(jobCategoryService.updateCategory(eq(1L), any(JobCategoryCmd.class))).thenReturn(response);

        mockMvc.perform(put("/api/job-categories").param("id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Cat"));
    }

    @Test
    void deleteCategory_shouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/job-categories").param("id", "1"))
                .andExpect(status().isNoContent());

        verify(jobCategoryService).deleteCategory(1L);
    }
}
