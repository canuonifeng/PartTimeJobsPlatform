package com.parttime.enterprise.service;

import com.parttime.enterprise.service.impl.WeChatTemplateServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatCode;

class WeChatTemplateServiceTest {

    private WeChatTemplateService weChatTemplateService;

    @BeforeEach
    void setUp() {
        weChatTemplateService = new WeChatTemplateServiceImpl();
    }

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
