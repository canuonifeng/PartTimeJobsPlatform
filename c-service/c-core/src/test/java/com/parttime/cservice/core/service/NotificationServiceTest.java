package com.parttime.cservice.core.service;

import com.parttime.cservice.core.dto.NotificationResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationServiceTest {

    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService();
    }

    @Test
    void sendNotification_shouldStoreAndReturnResponse() {
        NotificationResponse response = notificationService.sendNotification(
                100L, "APPLICATION_RECEIVED", "New Application", "You have a new application");

        assertThat(response).isNotNull();
        assertThat(response.getId()).isPositive();
        assertThat(response.getType()).isEqualTo("APPLICATION_RECEIVED");
        assertThat(response.getTitle()).isEqualTo("New Application");
        assertThat(response.getContent()).isEqualTo("You have a new application");
        assertThat(response.getStatus()).isEqualTo("PENDING");
        assertThat(response.getSentAt()).isNotNull();
    }

    @Test
    void getMyNotifications_shouldReturnNotificationsForWorker() {
        notificationService.sendNotification(100L, "APPLICATION_RECEIVED", "Title1", "Content1");
        notificationService.sendNotification(100L, "APPLICATION_STATUS", "Title2", "Content2");

        List<NotificationResponse> notifications = notificationService.getMyNotifications(100L);

        assertThat(notifications).hasSize(2);
        assertThat(notifications.get(0).getTitle()).isEqualTo("Title2");
        assertThat(notifications.get(1).getTitle()).isEqualTo("Title1");
    }

    @Test
    void getMyNotifications_shouldReturnEmptyListForUnknownWorker() {
        List<NotificationResponse> notifications = notificationService.getMyNotifications(999L);

        assertThat(notifications).isEmpty();
    }

    @Test
    void sendNotification_shouldIncrementId() {
        NotificationResponse r1 = notificationService.sendNotification(100L, "TYPE_A", "A", "A");
        NotificationResponse r2 = notificationService.sendNotification(100L, "TYPE_B", "B", "B");

        assertThat(r2.getId()).isGreaterThan(r1.getId());
    }

    @Test
    void getMyNotifications_shouldOnlyReturnOwnNotifications() {
        notificationService.sendNotification(100L, "TYPE", "Mine", "Mine");
        notificationService.sendNotification(200L, "TYPE", "Theirs", "Theirs");

        List<NotificationResponse> mine = notificationService.getMyNotifications(100L);

        assertThat(mine).hasSize(1);
        assertThat(mine.get(0).getTitle()).isEqualTo("Mine");
    }
}
