package com.parttime.enterprise.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TestRoleControllerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new TestRoleController()).build();
    }

    @Test
    void adminEndpoint_shouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/test/admin"))
                .andExpect(status().isOk())
                .andExpect(content().string("admin ok"));
    }

    @Test
    void hrEndpoint_shouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/test/hr"))
                .andExpect(status().isOk())
                .andExpect(content().string("hr ok"));
    }

    @Test
    void managerEndpoint_shouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/test/manager"))
                .andExpect(status().isOk())
                .andExpect(content().string("manager ok"));
    }

    @Test
    void financeEndpoint_shouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/test/finance"))
                .andExpect(status().isOk())
                .andExpect(content().string("finance ok"));
    }
}
