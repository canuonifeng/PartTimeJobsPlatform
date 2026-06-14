package com.parttime.enterprise.controller;

import com.parttime.enterprise.pojo.vo.JobTagGroupVO;
import com.parttime.enterprise.pojo.vo.JobTagVO;
import com.parttime.enterprise.service.JobTagService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class JobTagControllerTest {

    @Mock
    private JobTagService jobTagService;

    @InjectMocks
    private JobTagController jobTagController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jobTagController, "jobTagService", jobTagService);
        mockMvc = MockMvcBuilders.standaloneSetup(jobTagController).build();
    }

    @Test
    void getActiveTags_shouldReturnGroupsWithTags() throws Exception {
        JobTagVO tag = new JobTagVO();
        tag.setId(2L);
        tag.setName("日结");

        JobTagGroupVO group = new JobTagGroupVO();
        group.setId(1L);
        group.setName("结算周期");
        group.setTags(List.of(tag));

        when(jobTagService.getActiveGroups()).thenReturn(List.of(group));

        mockMvc.perform(get("/api/enterprise/job-tags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("结算周期"))
                .andExpect(jsonPath("$.data[0].tags[0].name").value("日结"));

        verify(jobTagService).getActiveGroups();
    }
}
