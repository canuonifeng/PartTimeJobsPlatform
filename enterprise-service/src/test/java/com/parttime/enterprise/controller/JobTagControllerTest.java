package com.parttime.enterprise.controller;

import com.parttime.enterprise.config.JwtTokenProvider;
import com.parttime.enterprise.config.SecurityConfig;
import com.parttime.enterprise.pojo.vo.JobTagGroupVO;
import com.parttime.enterprise.pojo.vo.JobTagVO;
import com.parttime.enterprise.service.JobTagService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(JobTagController.class)
@Import(SecurityConfig.class)
class JobTagControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JobTagService jobTagService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @WithMockUser(roles = "ADMIN")
    void getActiveTags_shouldReturnGroupsWithTags() throws Exception {
        JobTagVO tag = new JobTagVO();
        tag.setId(2L);
        tag.setName("日结");

        JobTagGroupVO group = new JobTagGroupVO();
        group.setId(1L);
        group.setName("结算周期");
        group.setTags(List.of(tag));

        when(jobTagService.getActiveGroups()).thenReturn(List.of(group));

        mockMvc.perform(get("/api/job-tags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("结算周期"))
                .andExpect(jsonPath("$[0].tags[0].name").value("日结"));

        verify(jobTagService).getActiveGroups();
    }
}
