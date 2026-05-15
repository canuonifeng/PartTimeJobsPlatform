package com.parttime.cservice.web.controller;

import com.parttime.cservice.core.dto.ProfileResponse;
import com.parttime.cservice.core.dto.ProfileUpdateRequest;
import com.parttime.cservice.core.dto.ResumeResponse;
import com.parttime.cservice.core.dto.ResumeUploadRequest;
import com.parttime.cservice.core.service.ProfileService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ProfileControllerTest {

    private MockMvc mockMvc;
    private ProfileService profileService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        profileService = mock(ProfileService.class);
        objectMapper = new ObjectMapper();

        ProfileController controller = new ProfileController(profileService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void getProfile_shouldReturnProfile() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("1", null, List.of()));

        ProfileResponse response = new ProfileResponse();
        response.setWorkerId(1L);
        response.setName("John");

        when(profileService.getProfile(1L)).thenReturn(response);

        mockMvc.perform(get("/api/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John"));
    }

    @Test
    void getProfile_shouldReturnUnauthorizedWhenNotAuthenticated() throws Exception {
        SecurityContextHolder.clearContext();
        mockMvc.perform(get("/api/profile"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateProfile_shouldReturnUpdatedProfile() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("1", null, List.of()));

        ProfileResponse response = new ProfileResponse();
        response.setWorkerId(1L);
        response.setName("John Updated");

        when(profileService.updateProfile(eq(1L), any())).thenReturn(response);

        ProfileUpdateRequest request = new ProfileUpdateRequest();
        request.setName("John Updated");
        request.setPhone("13900139000");

        mockMvc.perform(put("/api/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John Updated"));
    }

    @Test
    void uploadResume_shouldReturnCreated() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("1", null, List.of()));

        ResumeResponse response = new ResumeResponse();
        response.setId(1L);
        response.setFileName("resume.pdf");

        when(profileService.uploadResume(eq(1L), eq("resume.pdf"), eq("http://files/resume.pdf")))
                .thenReturn(response);

        ResumeUploadRequest request = new ResumeUploadRequest();
        request.setFileName("resume.pdf");
        request.setFileUrl("http://files/resume.pdf");

        mockMvc.perform(post("/api/profile/resumes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fileName").value("resume.pdf"));
    }

    @Test
    void getResumes_shouldReturnList() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("1", null, List.of()));

        ResumeResponse r1 = new ResumeResponse();
        r1.setId(1L);
        r1.setFileName("resume.pdf");

        when(profileService.getResumes(1L)).thenReturn(List.of(r1));

        mockMvc.perform(get("/api/profile/resumes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getProfile_shouldReturnBadRequestOnError() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("1", null, List.of()));

        when(profileService.getProfile(1L)).thenThrow(new RuntimeException("Profile not found"));

        mockMvc.perform(get("/api/profile"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Profile not found"));
    }
}
