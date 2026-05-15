package com.parttime.cservice.web.controller;

import com.parttime.cservice.core.dto.NotificationResponse;
import com.parttime.cservice.core.service.NotificationService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class NotificationControllerTest {

    private MockMvc mockMvc;
    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        notificationService = mock(NotificationService.class);
        NotificationController controller = new NotificationController(notificationService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        SecurityContextHolder.clearContext();
    }

    @Test
    void getMyNotifications_withAuth_shouldReturn200() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("1", null, List.of()));

        NotificationResponse response = new NotificationResponse();
        response.setId(1L);
        response.setType("APPLICATION_RECEIVED");
        response.setTitle("New Application");
        response.setContent("You have a new application");
        response.setStatus("SENT");
        response.setSentAt(LocalDateTime.now());

        when(notificationService.getMyNotifications(1L)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/notifications/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].type").value("APPLICATION_RECEIVED"))
                .andExpect(jsonPath("$[0].title").value("New Application"))
                .andExpect(jsonPath("$[0].status").value("SENT"));
    }

    @Test
    void getMyNotifications_withoutAuth_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/notifications/my"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getMyNotifications_withEmptyList_shouldReturn200() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("2", null, List.of()));

        when(notificationService.getMyNotifications(2L)).thenReturn(List.of());

        mockMvc.perform(get("/api/notifications/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
