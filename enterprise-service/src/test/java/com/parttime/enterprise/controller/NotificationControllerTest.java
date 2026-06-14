package com.parttime.enterprise.controller;

import com.parttime.enterprise.pojo.cmd.NotificationTemplateCmd;
import com.parttime.enterprise.pojo.entity.NotificationTemplate;
import com.parttime.enterprise.pojo.vo.NotificationLogVO;
import com.parttime.enterprise.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationController notificationController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(notificationController, "notificationService", notificationService);
        mockMvc = MockMvcBuilders.standaloneSetup(notificationController).build();
    }

    @Test
    void getNotifications_shouldReturnList() throws Exception {
        NotificationLogVO response = new NotificationLogVO();
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
    void getNotifications_withDefaultRecipientType() throws Exception {
        when(notificationService.getNotificationsByRecipient(100L, "ENTERPRISE"))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/notifications")
                        .param("recipientId", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
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
    void getTemplates_shouldReturnEmptyListWhenNone() throws Exception {
        when(notificationService.getNotificationTemplates("UNKNOWN", null)).thenReturn(List.of());

        mockMvc.perform(get("/api/notification-templates")
                        .param("type", "UNKNOWN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void createTemplate_shouldReturnCreated() throws Exception {
        NotificationTemplate created = new NotificationTemplate();
        created.setId(1L);
        created.setType("JOB_POSTED");
        created.setChannel("WECHAT_TEMPLATE");
        created.setTitleTemplate("New Job");
        created.setContentTemplate("Job {jobTitle} posted");

        when(notificationService.createNotificationTemplate(any())).thenReturn(created);

        NotificationTemplateCmd request = new NotificationTemplateCmd();
        request.setType("JOB_POSTED");
        request.setChannel("WECHAT_TEMPLATE");
        request.setTitleTemplate("New Job");
        request.setContentTemplate("Job {jobTitle} posted");

        mockMvc.perform(post("/api/notification-templates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.type").value("JOB_POSTED"));
    }

    @Test
    void updateTemplate_shouldReturnOk() throws Exception {
        NotificationTemplate updated = new NotificationTemplate();
        updated.setId(1L);
        updated.setTitleTemplate("Updated Title");
        updated.setContentTemplate("Updated Content");

        when(notificationService.updateNotificationTemplate(eq(1L), any())).thenReturn(updated);

        NotificationTemplateCmd request = new NotificationTemplateCmd();
        request.setTitleTemplate("Updated Title");
        request.setContentTemplate("Updated Content");

        request.setId(1L);

        mockMvc.perform(post("/api/notification-templates/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.titleTemplate").value("Updated Title"));
    }

    @Test
    void deleteTemplate_shouldReturnNoContent() throws Exception {
        mockMvc.perform(post("/api/notification-templates/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":1}"))
                .andExpect(status().isOk());
    }
}
