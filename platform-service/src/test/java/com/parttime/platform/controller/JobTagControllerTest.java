package com.parttime.platform.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.parttime.platform.pojo.cmd.JobTagCmd;
import com.parttime.platform.pojo.cmd.JobTagGroupCmd;
import com.parttime.platform.pojo.vo.JobTagGroupVO;
import com.parttime.platform.pojo.vo.JobTagVO;
import com.parttime.platform.service.JobTagService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
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
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
class JobTagControllerTest {

    private MockMvc mockMvc;

    @Mock
    private JobTagService jobTagService;

    @InjectMocks
    private JobTagController jobTagController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = standaloneSetup(jobTagController).build();
    }

    @Test
    void getAllGroups_shouldReturnNestedTags() throws Exception {
        JobTagVO tag = new JobTagVO();
        tag.setId(2L);
        tag.setName("Daily");

        JobTagGroupVO group = new JobTagGroupVO();
        group.setId(1L);
        group.setName("Settlement Cycle");
        group.setTags(List.of(tag));

        when(jobTagService.getAllGroups()).thenReturn(List.of(group));

        mockMvc.perform(get("/api/admin/job-tags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("Settlement Cycle"))
                .andExpect(jsonPath("$.data[0].tags[0].name").value("Daily"));
    }

    @Test
    void createGroup_shouldReturnCreated() throws Exception {
        JobTagGroupCmd cmd = new JobTagGroupCmd();
        cmd.setName("Settlement Cycle");
        cmd.setCode("settlement_cycle");

        JobTagGroupVO response = new JobTagGroupVO();
        response.setId(1L);
        response.setName("Settlement Cycle");

        when(jobTagService.createGroup(any(JobTagGroupCmd.class))).thenReturn(response);

        mockMvc.perform(post("/api/admin/job-tag-groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cmd)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Settlement Cycle"));
    }

    @Test
    void updateGroup_shouldReturnOk() throws Exception {
        JobTagGroupCmd cmd = new JobTagGroupCmd();
        cmd.setName("Updated Group");

        JobTagGroupVO response = new JobTagGroupVO();
        response.setId(1L);
        response.setName("Updated Group");

        when(jobTagService.updateGroup(eq(1L), any(JobTagGroupCmd.class))).thenReturn(response);

        cmd.setId(1L);

        mockMvc.perform(post("/api/admin/job-tag-groups/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cmd)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Updated Group"));
    }

    @Test
    void deleteGroup_shouldReturnNoContent() throws Exception {
        mockMvc.perform(post("/api/admin/job-tag-groups/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":1}"))
                .andExpect(status().isOk());

        verify(jobTagService).deleteGroup(1L);
    }

    @Test
    void createTag_shouldReturnCreated() throws Exception {
        JobTagCmd cmd = new JobTagCmd();
        cmd.setGroupId(1L);
        cmd.setName("Daily");
        cmd.setCode("daily");

        JobTagVO response = new JobTagVO();
        response.setId(2L);
        response.setName("Daily");

        when(jobTagService.createTag(any(JobTagCmd.class))).thenReturn(response);

        mockMvc.perform(post("/api/admin/job-tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cmd)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Daily"));
    }

    @Test
    void updateTag_shouldReturnOk() throws Exception {
        JobTagCmd cmd = new JobTagCmd();
        cmd.setName("Updated Tag");

        JobTagVO response = new JobTagVO();
        response.setId(2L);
        response.setName("Updated Tag");

        when(jobTagService.updateTag(eq(2L), any(JobTagCmd.class))).thenReturn(response);

        cmd.setId(2L);

        mockMvc.perform(post("/api/admin/job-tags/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cmd)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Updated Tag"));
    }

    @Test
    void deleteTag_shouldReturnNoContent() throws Exception {
        mockMvc.perform(post("/api/admin/job-tags/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":2}"))
                .andExpect(status().isOk());

        verify(jobTagService).deleteTag(2L);
    }
}
