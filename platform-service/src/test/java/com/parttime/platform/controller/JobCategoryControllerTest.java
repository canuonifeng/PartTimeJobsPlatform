package com.parttime.platform.controller;

import com.parttime.platform.config.JwtTokenProvider;
import com.parttime.platform.config.SecurityConfig;
import com.parttime.platform.pojo.cmd.JobCategoryCmd;
import com.parttime.platform.pojo.vo.JobCategoryVO;
import com.parttime.platform.service.JobCategoryService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
        JobCategoryVO cat = new JobCategoryVO();
        cat.setId(1L);
        cat.setName("Parent");

        when(jobCategoryService.getAllCategories()).thenReturn(List.of(cat));

        mockMvc.perform(get("/api/admin/job-categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("Parent"));

        verify(jobCategoryService).getAllCategories();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createCategory_shouldReturnCreated() throws Exception {
        JobCategoryCmd cmd = new JobCategoryCmd();
        cmd.setName("New Cat");
        cmd.setSortOrder(1);

        JobCategoryVO response = new JobCategoryVO();
        response.setId(1L);
        response.setName("New Cat");
        response.setSortOrder(1);

        when(jobCategoryService.createCategory(any(JobCategoryCmd.class))).thenReturn(response);

        mockMvc.perform(post("/api/admin/job-categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cmd)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("New Cat"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateCategory_shouldReturnOk() throws Exception {
        JobCategoryCmd cmd = new JobCategoryCmd();
        cmd.setId(1L);
        cmd.setName("Updated Cat");

        JobCategoryVO response = new JobCategoryVO();
        response.setId(1L);
        response.setName("Updated Cat");

        when(jobCategoryService.updateCategory(eq(1L), any(JobCategoryCmd.class))).thenReturn(response);

        mockMvc.perform(post("/api/admin/job-categories/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cmd)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Updated Cat"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteCategory_shouldReturnNoContent() throws Exception {
        mockMvc.perform(post("/api/admin/job-categories/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":1}"))
                .andExpect(status().isOk());

        verify(jobCategoryService).deleteCategory(1L);
    }

}
