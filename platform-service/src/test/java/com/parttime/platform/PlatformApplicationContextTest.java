package com.parttime.platform;

import com.parttime.platform.config.FeishuRobotProperties;
import com.parttime.platform.service.notification.FeishuRobotLeadNotifier;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

class PlatformApplicationContextTest {

    @Test
    void feishuRobotLeadNotifier_shouldBeCreatedBySpringContext() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.registerBean(FeishuRobotProperties.class);
            context.register(FeishuRobotLeadNotifier.class);
            context.refresh();

            assertThat(context.getBean(FeishuRobotLeadNotifier.class)).isNotNull();
        }
    }
}
