package com.parttime.platform.web.controller;

import com.parttime.platform.api.dto.RegistrationListResponse;
import com.parttime.platform.api.dto.RegistrationResponse;
import com.parttime.platform.core.auth.JwtTokenProvider;
import com.parttime.platform.core.service.RegistrationService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RegistrationController.class)
@Import(SecurityConfig.class)
class RegistrationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RegistrationService registrationService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "ADMIN")
    void listRegistrations_shouldReturnList() throws Exception {
        RegistrationListResponse listResponse = new RegistrationListResponse();
        when(registrationService.getRegistrations(null)).thenReturn(listResponse);

        mockMvc.perform(get("/api/registrations"))
                .andExpect(status().isOk());

        verify(registrationService).getRegistrations(null);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void listRegistrations_withStatus_shouldReturnFiltered() throws Exception {
        RegistrationListResponse listResponse = new RegistrationListResponse();
        when(registrationService.getRegistrations("PENDING")).thenReturn(listResponse);

        mockMvc.perform(get("/api/registrations?status=PENDING"))
                .andExpect(status().isOk());

        verify(registrationService).getRegistrations("PENDING");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getRegistration_shouldReturnDetail() throws Exception {
        RegistrationResponse response = new RegistrationResponse();
        response.setId(1L);
        response.setCompanyName("TestCo");
        response.setStatus("PENDING");

        when(registrationService.getRegistration(1L)).thenReturn(response);

        mockMvc.perform(get("/api/registrations/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.companyName").value("TestCo"));

        verify(registrationService).getRegistration(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void approveRegistration_shouldReturnOk() throws Exception {
        RegistrationResponse response = new RegistrationResponse();
        response.setId(1L);
        response.setStatus("APPROVED");

        when(registrationService.approveRegistration(eq(1L), any())).thenReturn(response);

        mockMvc.perform(put("/api/registrations/1/approve"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));

        verify(registrationService).approveRegistration(eq(1L), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void rejectRegistration_shouldReturnOk() throws Exception {
        RegistrationResponse response = new RegistrationResponse();
        response.setId(1L);
        response.setStatus("REJECTED");
        response.setReviewRemark("Invalid docs");

        when(registrationService.rejectRegistration(eq(1L), any(), any())).thenReturn(response);

        String json = "{\"remark\":\"Invalid docs\"}";

        mockMvc.perform(put("/api/registrations/1/reject")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"))
                .andExpect(jsonPath("$.reviewRemark").value("Invalid docs"));

        verify(registrationService).rejectRegistration(eq(1L), any(), any());
    }

    @Test
    void listRegistrations_withoutAuth_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/registrations"))
                .andExpect(status().isUnauthorized());
    }
}
