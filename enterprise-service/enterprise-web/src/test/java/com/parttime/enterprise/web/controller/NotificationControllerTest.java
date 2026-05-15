package com.parttime.enterprise.web.controller;

import com.parttime.enterprise.api.dto.NotificationLogResponse;
import com.parttime.enterprise.api.dto.NotificationTemplateRequest;
import com.parttime.enterprise.core.auth.JwtTokenProvider;
import com.parttime.enterprise.core.domain.NotificationTemplate;
import com.parttime.enterprise.core.service.NotificationService;
import com.parttime.enterprise.web.config.SecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationController.class)
@Import(SecurityConfig.class)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @WithMockUser(roles = "ADMIN")
    void getNotifications_shouldReturnList() throws Exception {
        NotificationLogResponse response = new NotificationLogResponse();
        response.setId(1L);
        response.setRecipientId(100L);
        response.setRecipientType("ENTERPRISE");
        response.setType("APPLICATION_RECEIVED");
        response.setChannel("IN_APP");
        response.setTitle("New Application");
        response.setContent("You have a new application");
        response.setStatus("SENT");
        response.setSentAt(LocalDateTime.now());

        when(notificationService.getNotificationsByRecipient(100L, "ENTERPRISE"))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/notifications")
                        .param("recipientId", "100")
                        .param("recipientType", "ENTERPRISE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].recipientId").value(100))
                .andExpect(jsonPath("$[0].status").value("SENT"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getNotifications_withDefaultRecipientType() throws Exception {
        when(notificationService.getNotificationsByRecipient(100L, "ENTERPRISE"))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/notifications")
                        .param("recipientId", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getTemplates_shouldReturnList() throws Exception {
        NotificationTemplate template = new NotificationTemplate();
        template.setId(1L);
        template.setType("APPLICATION_RECEIVED");
        template.setChannel("IN_APP");
        template.setTitleTemplate("New Application");
        template.setContentTemplate("You have a new application");

        when(notificationService.getNotificationTemplates("APPLICATION_RECEIVED", null))
                .thenReturn(List.of(template));

        mockMvc.perform(get("/api/notification-templates")
                        .param("type", "APPLICATION_RECEIVED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].type").value("APPLICATION_RECEIVED"))
                .andExpect(jsonPath("$[0].titleTemplate").value("New Application"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getTemplates_shouldReturnEmptyListWhenNone() throws Exception {
        when(notificationService.getNotificationTemplates("UNKNOWN", null)).thenReturn(List.of());

        mockMvc.perform(get("/api/notification-templates")
                        .param("type", "UNKNOWN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createTemplate_shouldReturnCreated() throws Exception {
        NotificationTemplate created = new NotificationTemplate();
        created.setId(1L);
        created.setType("JOB_POSTED");
        created.setChannel("WECHAT_TEMPLATE");
        created.setTitleTemplate("New Job");
        created.setContentTemplate("Job {jobTitle} posted");

        when(notificationService.createNotificationTemplate(any())).thenReturn(created);

        NotificationTemplateRequest request = new NotificationTemplateRequest();
        request.setType("JOB_POSTED");
        request.setChannel("WECHAT_TEMPLATE");
        request.setTitleTemplate("New Job");
        request.setContentTemplate("Job {jobTitle} posted");

        mockMvc.perform(post("/api/notification-templates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.type").value("JOB_POSTED"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateTemplate_shouldReturnOk() throws Exception {
        NotificationTemplate updated = new NotificationTemplate();
        updated.setId(1L);
        updated.setTitleTemplate("Updated Title");
        updated.setContentTemplate("Updated Content");

        when(notificationService.updateNotificationTemplate(eq(1L), any())).thenReturn(updated);

        NotificationTemplateRequest request = new NotificationTemplateRequest();
        request.setTitleTemplate("Updated Title");
        request.setContentTemplate("Updated Content");

        mockMvc.perform(put("/api/notification-templates/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titleTemplate").value("Updated Title"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteTemplate_shouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/notification-templates/1"))
                .andExpect(status().isNoContent());
    }
}
