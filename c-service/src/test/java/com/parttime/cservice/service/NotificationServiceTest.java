package com.parttime.cservice.service;

import com.parttime.cservice.service.impl.NotificationServiceImpl;
import com.parttime.cservice.pojo.vo.NotificationVO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationServiceTest {

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(notificationService, "notificationMapper", InMemoryMappers.createNotificationMapper());
    }

    @Test
    void sendNotification_shouldStoreAndReturnResponse() {
        NotificationVO response = notificationService.sendNotification(
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

        List<NotificationVO> notifications = notificationService.getMyNotifications(100L);

        assertThat(notifications).hasSize(2);
        assertThat(notifications.get(0).getTitle()).isEqualTo("Title2");
        assertThat(notifications.get(1).getTitle()).isEqualTo("Title1");
    }

    @Test
    void getMyNotifications_shouldReturnEmptyListForUnknownWorker() {
        List<NotificationVO> notifications = notificationService.getMyNotifications(999L);

        assertThat(notifications).isEmpty();
    }

    @Test
    void sendNotification_shouldIncrementId() {
        NotificationVO r1 = notificationService.sendNotification(100L, "TYPE_A", "A", "A");
        NotificationVO r2 = notificationService.sendNotification(100L, "TYPE_B", "B", "B");

        assertThat(r2.getId()).isGreaterThan(r1.getId());
    }

    @Test
    void getMyNotifications_shouldOnlyReturnOwnNotifications() {
        notificationService.sendNotification(100L, "TYPE", "Mine", "Mine");
        notificationService.sendNotification(200L, "TYPE", "Theirs", "Theirs");

        List<NotificationVO> mine = notificationService.getMyNotifications(100L);

        assertThat(mine).hasSize(1);
        assertThat(mine.get(0).getTitle()).isEqualTo("Mine");
    }
}
