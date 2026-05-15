package com.parttime.platform.web.controller;

import com.parttime.platform.api.dto.JobCategoryRequest;
import com.parttime.platform.api.dto.JobCategoryResponse;
import com.parttime.platform.core.auth.JwtTokenProvider;
import com.parttime.platform.core.service.JobCategoryService;
import com.parttime.platform.web.config.SecurityConfig;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
        JobCategoryResponse cat = new JobCategoryResponse();
        cat.setId(1L);
        cat.setName("Parent");

        when(jobCategoryService.getAllCategories()).thenReturn(List.of(cat));

        mockMvc.perform(get("/api/admin/job-categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Parent"));

        verify(jobCategoryService).getAllCategories();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createCategory_shouldReturnCreated() throws Exception {
        JobCategoryRequest request = new JobCategoryRequest();
        request.setName("New Cat");
        request.setSortOrder(1);

        JobCategoryResponse response = new JobCategoryResponse();
        response.setId(1L);
        response.setName("New Cat");
        response.setSortOrder(1);

        when(jobCategoryService.createCategory(any(JobCategoryRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/admin/job-categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("New Cat"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateCategory_shouldReturnOk() throws Exception {
        JobCategoryRequest request = new JobCategoryRequest();
        request.setName("Updated Cat");

        JobCategoryResponse response = new JobCategoryResponse();
        response.setId(1L);
        response.setName("Updated Cat");

        when(jobCategoryService.updateCategory(eq(1L), any(JobCategoryRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/admin/job-categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Cat"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteCategory_shouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/admin/job-categories/1"))
                .andExpect(status().isNoContent());

        verify(jobCategoryService).deleteCategory(1L);
    }

}
