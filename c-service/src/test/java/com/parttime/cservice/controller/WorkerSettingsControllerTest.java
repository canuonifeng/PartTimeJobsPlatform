package com.parttime.cservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.parttime.cservice.mapper.WorkerSettingsMapper;
import com.parttime.cservice.pojo.cmd.UpdateWorkerSettingsCmd;
import com.parttime.cservice.pojo.entity.WorkerSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class WorkerSettingsControllerTest {
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private WorkerSettingsMapper workerSettingsMapper;

    @InjectMocks
    private WorkerSettingsController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        SecurityContextHolder.clearContext();
    }

    @Test
    void getSettings_withoutExistingRow_shouldReturnDefaults() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("1", null, List.of()));
        when(workerSettingsMapper.findByWorkerId(1L)).thenReturn(null);

        mockMvc.perform(get("/api/settings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pushEnabled").value(true))
                .andExpect(jsonPath("$.locationEnabled").value(true))
                .andExpect(jsonPath("$.quietEnabled").value(false));
    }

    @Test
    void updateSettings_shouldPersistMergedValues() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("1", null, List.of()));
        WorkerSettings existing = new WorkerSettings();
        existing.setPushEnabled(true);
        existing.setLocationEnabled(true);
        existing.setQuietEnabled(false);
        when(workerSettingsMapper.findByWorkerId(1L)).thenReturn(existing);
        when(workerSettingsMapper.upsert(eq(1L), eq(false), eq(true), eq(true))).thenReturn(1);

        UpdateWorkerSettingsCmd cmd = new UpdateWorkerSettingsCmd();
        cmd.setPushEnabled(false);
        cmd.setQuietEnabled(true);

        mockMvc.perform(put("/api/settings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cmd)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pushEnabled").value(false))
                .andExpect(jsonPath("$.locationEnabled").value(true))
                .andExpect(jsonPath("$.quietEnabled").value(true));
    }

    @Test
    void getSettings_withoutAuth_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/settings"))
                .andExpect(status().isUnauthorized());
    }
}
