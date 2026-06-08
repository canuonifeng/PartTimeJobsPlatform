package com.parttime.cservice.controller;

import com.parttime.cservice.pojo.vo.NotificationVO;
import com.parttime.cservice.pojo.vo.PageVO;
import com.parttime.cservice.service.impl.NotificationServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class NotificationControllerTest {

    private MockMvc mockMvc;
    @Mock
    private NotificationServiceImpl notificationService;
    @InjectMocks
    private NotificationController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        SecurityContextHolder.clearContext();
    }

    @Test
    void getMyNotifications_withAuth_shouldReturn200() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("1", null, List.of()));

        NotificationVO response = new NotificationVO();
        response.setId(1L);
        response.setType("APPLICATION_RECEIVED");
        response.setTitle("New Application");
        response.setContent("You have a new application");
        response.setStatus("SENT");
        response.setCategory("application");
        response.setRead(false);
        response.setRelatedType("APPLICATION");
        response.setRelatedId(10L);
        response.setSentAt(LocalDateTime.now());

        when(notificationService.getMyNotifications(1L, 1, 20)).thenReturn(new PageVO<>(List.of(response), 1));

        mockMvc.perform(get("/api/notifications/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records.length()").value(1))
                .andExpect(jsonPath("$.data.records[0].id").value(1))
                .andExpect(jsonPath("$.data.records[0].type").value("APPLICATION_RECEIVED"))
                .andExpect(jsonPath("$.data.records[0].category").value("application"))
                .andExpect(jsonPath("$.data.records[0].title").value("New Application"))
                .andExpect(jsonPath("$.data.records[0].read").value(false))
                .andExpect(jsonPath("$.data.records[0].relatedType").value("APPLICATION"))
                .andExpect(jsonPath("$.data.records[0].relatedId").value(10))
                .andExpect(jsonPath("$.data.records[0].status").value("SENT"));
    }

    @Test
    void markNotificationRead_withOwner_shouldReturn200() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("1", null, List.of()));

        mockMvc.perform(put("/api/notifications/10/read"))
                .andExpect(status().isOk());

        verify(notificationService).markAsRead(1L, 10L);
    }

    @Test
    void getMyNotifications_withoutAuth_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/notifications/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void getMyNotifications_withEmptyList_shouldReturn200() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("2", null, List.of()));

        when(notificationService.getMyNotifications(2L, 1, 20)).thenReturn(new PageVO<>(List.of(), 0));

        mockMvc.perform(get("/api/notifications/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.records.length()").value(0))
                .andExpect(jsonPath("$.data.total").value(0));
    }
}
