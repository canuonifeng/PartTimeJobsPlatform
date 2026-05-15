package com.parttime.enterprise.service;

import com.parttime.enterprise.dao.NotificationLogDao;
import com.parttime.enterprise.dao.NotificationTemplateDao;
import com.parttime.enterprise.pojo.cmd.NotificationTemplateCmd;
import com.parttime.enterprise.pojo.entity.NotificationLog;
import com.parttime.enterprise.pojo.entity.NotificationTemplate;
import com.parttime.enterprise.pojo.vo.NotificationLogVO;
import com.parttime.enterprise.service.impl.NotificationServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationLogDao notificationLogDao;

    @Mock
    private NotificationTemplateDao notificationTemplateDao;

    @Captor
    private ArgumentCaptor<NotificationLog> logCaptor;

    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationServiceImpl(notificationLogDao, notificationTemplateDao);
    }

    @Test
    void sendNotification_shouldCreateLogWithPendingStatus() {
        doAnswer(invocation -> {
            NotificationLog log = invocation.getArgument(0);
            log.setId(1L);
            return null;
        }).when(notificationLogDao).save(any(NotificationLog.class));

        NotificationLogVO response = notificationService.sendNotification(
                100L, "WORKER", "APPLICATION_RECEIVED", "IN_APP",
                "New Application", "You have a new application");

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getRecipientId()).isEqualTo(100L);
        assertThat(response.getRecipientType()).isEqualTo("WORKER");
        assertThat(response.getType()).isEqualTo("APPLICATION_RECEIVED");
        assertThat(response.getChannel()).isEqualTo("IN_APP");
        assertThat(response.getTitle()).isEqualTo("New Application");
        assertThat(response.getContent()).isEqualTo("You have a new application");
        assertThat(response.getStatus()).isEqualTo("PENDING");

        verify(notificationLogDao).save(logCaptor.capture());
        NotificationLog saved = logCaptor.getValue();
        assertThat(saved.getRecipientId()).isEqualTo(100L);
        assertThat(saved.getStatus()).isEqualTo("PENDING");
    }

    @Test
    void getNotificationsByRecipient_shouldReturnLogs() {
        NotificationLog log = new NotificationLog();
        log.setId(1L);
        log.setRecipientId(200L);
        log.setRecipientType("ENTERPRISE");
        log.setType("APPLICATION_RECEIVED");
        log.setChannel("IN_APP");
        log.setTitle("Test");
        log.setContent("Test content");
        log.setStatus("SENT");
        log.setSentAt(LocalDateTime.now());

        when(notificationLogDao.findByRecipientIdAndRecipientType(200L, "ENTERPRISE"))
                .thenReturn(List.of(log));

        List<NotificationLogVO> responses = notificationService.getNotificationsByRecipient(200L, "ENTERPRISE");

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getId()).isEqualTo(1L);
        assertThat(responses.get(0).getStatus()).isEqualTo("SENT");
        assertThat(responses.get(0).getSentAt()).isNotNull();
    }

    @Test
    void getNotificationsByRecipient_shouldReturnEmptyListWhenNone() {
        when(notificationLogDao.findByRecipientIdAndRecipientType(999L, "WORKER"))
                .thenReturn(List.of());

        List<NotificationLogVO> responses = notificationService.getNotificationsByRecipient(999L, "WORKER");

        assertThat(responses).isEmpty();
    }

    @Test
    void markAsSent_shouldUpdateStatus() {
        notificationService.markAsSent(1L);
        verify(notificationLogDao).markSent(any(), any(LocalDateTime.class));
    }

    @Test
    void markAsFailed_shouldUpdateStatusWithError() {
        notificationService.markAsFailed(1L, "Connection timeout");
        verify(notificationLogDao).updateStatusWithError(1L, "FAILED", "Connection timeout");
    }

    @Test
    void getTemplatesByType_shouldReturnTemplates() {
        NotificationTemplate template = new NotificationTemplate();
        template.setId(1L);
        template.setType("APPLICATION_RECEIVED");
        template.setChannel("IN_APP");
        template.setTitleTemplate("New Application");
        template.setContentTemplate("You have a new application from {workerName}");

        when(notificationTemplateDao.findByType("APPLICATION_RECEIVED"))
                .thenReturn(List.of(template));

        List<NotificationTemplate> templates = notificationService.getTemplatesByType("APPLICATION_RECEIVED");

        assertThat(templates).hasSize(1);
        assertThat(templates.get(0).getTitleTemplate()).isEqualTo("New Application");
    }

    @Test
    void getTemplatesByType_shouldReturnEmptyListWhenNone() {
        when(notificationTemplateDao.findByType("UNKNOWN")).thenReturn(List.of());

        List<NotificationTemplate> templates = notificationService.getTemplatesByType("UNKNOWN");

        assertThat(templates).isEmpty();
    }

    @Test
    void createNotificationTemplate_shouldSaveAndReturn() {
        NotificationTemplateCmd request = new NotificationTemplateCmd();
        request.setType("JOB_POSTED");
        request.setChannel("WECHAT_TEMPLATE");
        request.setTitleTemplate("New Job Posted");
        request.setContentTemplate("A new job {jobTitle} has been posted");

        doAnswer(invocation -> {
            NotificationTemplate t = invocation.getArgument(0);
            t.setId(10L);
            return null;
        }).when(notificationTemplateDao).save(any(NotificationTemplate.class));

        NotificationTemplate result = notificationService.createNotificationTemplate(request);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getType()).isEqualTo("JOB_POSTED");
        assertThat(result.getChannel()).isEqualTo("WECHAT_TEMPLATE");
        assertThat(result.getTitleTemplate()).isEqualTo("New Job Posted");

        verify(notificationTemplateDao).save(any(NotificationTemplate.class));
    }

    @Test
    void updateNotificationTemplate_shouldUpdateFields() {
        NotificationTemplate existing = new NotificationTemplate();
        existing.setId(5L);
        existing.setType("JOB_POSTED");
        existing.setChannel("IN_APP");
        existing.setTitleTemplate("Old Title");
        existing.setContentTemplate("Old Content");

        when(notificationTemplateDao.findById(5L)).thenReturn(Optional.of(existing));

        NotificationTemplateCmd request = new NotificationTemplateCmd();
        request.setTitleTemplate("Updated Title");
        request.setContentTemplate("Updated Content");

        NotificationTemplate result = notificationService.updateNotificationTemplate(5L, request);

        assertThat(result.getTitleTemplate()).isEqualTo("Updated Title");
        assertThat(result.getContentTemplate()).isEqualTo("Updated Content");
        verify(notificationTemplateDao).update(existing);
    }

    @Test
    void updateNotificationTemplate_shouldThrowWhenNotFound() {
        when(notificationTemplateDao.findById(999L)).thenReturn(Optional.empty());

        NotificationTemplateCmd request = new NotificationTemplateCmd();
        request.setTitleTemplate("Title");
        request.setContentTemplate("Content");

        assertThatThrownBy(() -> notificationService.updateNotificationTemplate(999L, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void deleteNotificationTemplate_shouldDelete() {
        notificationService.deleteNotificationTemplate(1L);

        verify(notificationTemplateDao).deleteById(1L);
    }

    @Test
    void getNotificationTemplates_shouldReturnFiltered() {
        NotificationTemplate t1 = new NotificationTemplate();
        t1.setId(1L);
        t1.setType("JOB_POSTED");

        when(notificationTemplateDao.findAll("JOB_POSTED", null)).thenReturn(List.of(t1));

        List<NotificationTemplate> results = notificationService.getNotificationTemplates("JOB_POSTED", null);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getType()).isEqualTo("JOB_POSTED");
    }
}
