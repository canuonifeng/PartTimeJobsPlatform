package com.parttime.cservice.service;

import com.parttime.cservice.service.impl.NotificationServiceImpl;
import com.parttime.cservice.pojo.vo.NotificationVO;
import com.parttime.cservice.pojo.vo.PageVO;

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
        assertThat(response.getStatus()).isEqualTo("SENT");
        assertThat(response.getCategory()).isEqualTo("application");
        assertThat(response.getRead()).isFalse();
        assertThat(response.getSentAt()).isNotNull();
    }

    @Test
    void getMyNotifications_shouldReturnNotificationsForWorker() {
        notificationService.sendNotification(100L, "APPLICATION_RECEIVED", "Title1", "Content1");
        notificationService.sendNotification(100L, "APPLICATION_STATUS", "Title2", "Content2");

        PageVO<NotificationVO> page = notificationService.getMyNotifications(100L, 1, 20);
        List<NotificationVO> notifications = page.getRecords();

        assertThat(page.getTotal()).isEqualTo(2);
        assertThat(notifications).hasSize(2);
        assertThat(notifications.get(0).getTitle()).isEqualTo("Title2");
        assertThat(notifications.get(1).getTitle()).isEqualTo("Title1");
    }

    @Test
    void getMyNotifications_shouldReturnEmptyListForUnknownWorker() {
        PageVO<NotificationVO> page = notificationService.getMyNotifications(999L, 1, 20);

        assertThat(page.getRecords()).isEmpty();
        assertThat(page.getTotal()).isZero();
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

        List<NotificationVO> mine = notificationService.getMyNotifications(100L, 1, 20).getRecords();

        assertThat(mine).hasSize(1);
        assertThat(mine.get(0).getTitle()).isEqualTo("Mine");
    }

    @Test
    void getMyNotifications_shouldPageAndSortBySentTimeDescending() {
        notificationService.sendNotification(100L, "TYPE", "Old", "Old");
        notificationService.sendNotification(100L, "TYPE", "Middle", "Middle");
        notificationService.sendNotification(100L, "TYPE", "New", "New");

        PageVO<NotificationVO> page = notificationService.getMyNotifications(100L, 2, 1);

        assertThat(page.getTotal()).isEqualTo(3);
        assertThat(page.getRecords()).hasSize(1);
        assertThat(page.getRecords().get(0).getTitle()).isEqualTo("Middle");
    }

    @Test
    void createShiftStartReminder_shouldCreateScheduleNotification() {
        NotificationVO response = notificationService.createShiftStartReminder(
                100L,
                200L,
                "测试岗位",
                "2026-06-08",
                "09:00");

        assertThat(response.getType()).isEqualTo("SCHEDULE_START_REMINDER");
        assertThat(response.getCategory()).isEqualTo("schedule");
        assertThat(response.getTitle()).isEqualTo("开工提醒");
        assertThat(response.getContent()).isEqualTo("您报名的测试岗位将于2026-06-08 09:00开工，请按时到岗");
        assertThat(response.getRelatedType()).isEqualTo("SCHEDULE_SHIFT");
        assertThat(response.getRelatedId()).isEqualTo(200L);
        assertThat(response.getRead()).isFalse();
    }
}
