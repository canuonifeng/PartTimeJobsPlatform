package com.parttime.enterprise.controller;

import com.parttime.enterprise.config.JwtTokenProvider;
import com.parttime.enterprise.config.SecurityConfig;
import com.parttime.enterprise.pojo.cmd.JobCategoryCmd;
import com.parttime.enterprise.pojo.vo.JobCategoryVO;
import com.parttime.enterprise.service.JobCategoryService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(JobCategoryController.class)
@Import(SecurityConfig.class)
class JobCategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JobCategoryService jobCategoryService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "ADMIN")
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
    @WithMockUser(roles = "ADMIN")
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
    @WithMockUser(roles = "ADMIN")
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
    @WithMockUser(roles = "ADMIN")
    void deleteCategory_shouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/job-categories").param("id", "1"))
                .andExpect(status().isNoContent());

        verify(jobCategoryService).deleteCategory(1L);
    }
}
