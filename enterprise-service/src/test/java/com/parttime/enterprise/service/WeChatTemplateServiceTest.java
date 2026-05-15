package com.parttime.enterprise.service;

import com.parttime.enterprise.service.impl.WeChatTemplateServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatCode;

@ExtendWith(MockitoExtension.class)
class WeChatTemplateServiceTest {

    @InjectMocks
    private WeChatTemplateServiceImpl weChatTemplateService;

    @Test
    void sendTemplateMessage_shouldNotThrow() {
        assertThatCode(() -> weChatTemplateService.sendTemplateMessage(
                "o123456", "template_001", Map.of("name", "John")))
                .doesNotThrowAnyException();
    }

    @Test
    void sendTemplateMessage_shouldHandleEmptyData() {
        assertThatCode(() -> weChatTemplateService.sendTemplateMessage(
                "o123456", "template_002", Map.of()))
                .doesNotThrowAnyException();
    }

    @Test
    void sendTemplateMessage_shouldHandleNullData() {
        assertThatCode(() -> weChatTemplateService.sendTemplateMessage(
                "o123456", "template_003", null))
                .doesNotThrowAnyException();
    }
}
