package com.parttime.enterprise.web.controller;

import com.parttime.enterprise.core.auth.JwtTokenProvider;
import com.parttime.enterprise.web.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TestRoleController.class)
@Import(SecurityConfig.class)
class TestRoleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void adminShouldAccessAdminEndpoint() throws Exception {
        mockMvc.perform(get("/api/test/admin"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void adminShouldAccessHrEndpoint() throws Exception {
        mockMvc.perform(get("/api/test/hr"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void adminShouldAccessManagerEndpoint() throws Exception {
        mockMvc.perform(get("/api/test/manager"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void adminShouldAccessFinanceEndpoint() throws Exception {
        mockMvc.perform(get("/api/test/finance"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "hr", roles = "HR")
    void hrShouldAccessHrEndpoint() throws Exception {
        mockMvc.perform(get("/api/test/hr"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "hr", roles = "HR")
    void hrShouldNotAccessFinanceEndpoint() throws Exception {
        mockMvc.perform(get("/api/test/finance"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "finance", roles = "FINANCE")
    void financeShouldAccessFinanceEndpoint() throws Exception {
        mockMvc.perform(get("/api/test/finance"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "finance", roles = "FINANCE")
    void financeShouldNotAccessHrEndpoint() throws Exception {
        mockMvc.perform(get("/api/test/hr"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "manager", roles = "MANAGER")
    void managerShouldAccessManagerEndpoint() throws Exception {
        mockMvc.perform(get("/api/test/manager"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "manager", roles = "MANAGER")
    void managerShouldNotAccessAdminEndpoint() throws Exception {
        mockMvc.perform(get("/api/test/admin"))
                .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticatedShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/test/admin"))
                .andExpect(status().isUnauthorized());
    }
}
