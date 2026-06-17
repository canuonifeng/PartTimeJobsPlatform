package com.parttime.platform.service.notification;

import com.parttime.platform.config.FeishuRobotProperties;
import com.parttime.platform.pojo.entity.Lead;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class FeishuRobotLeadNotifierTest {

    @Test
    void notifyLeadCreated_disabledOrBlankWebhook_shouldSkipSend() {
        FeishuRobotProperties properties = new FeishuRobotProperties();
        properties.setEnabled(true);
        properties.setWebhookUrl(" ");
        RestTemplate restTemplate = mock(RestTemplate.class);
        FeishuRobotLeadNotifier notifier = new FeishuRobotLeadNotifier(properties, restTemplate);

        notifier.notifyLeadCreated(sampleLead());

        verify(restTemplate, never()).postForEntity(any(String.class), any(), eq(String.class));
    }

    @Test
    void notifyLeadCreated_enabledWebhook_shouldSendTextMessage() {
        FeishuRobotProperties properties = new FeishuRobotProperties();
        properties.setEnabled(true);
        properties.setWebhookUrl("https://open.feishu.cn/open-apis/bot/v2/hook/token");
        properties.setSecret("secret");
        RestTemplate restTemplate = mock(RestTemplate.class);
        FeishuRobotLeadNotifier notifier = new FeishuRobotLeadNotifier(properties, restTemplate);

        notifier.notifyLeadCreated(sampleLead());

        verify(restTemplate).postForEntity(eq(properties.getWebhookUrl()), org.mockito.ArgumentMatchers.<HttpEntity<Map<String, Object>>>argThat(entity -> {
            Map<String, Object> body = entity.getBody();
            if (body == null) {
                return false;
            }
            Object content = body.get("content");
            if (!(content instanceof Map<?, ?> contentMap)) {
                return false;
            }
            Object text = contentMap.get("text");
            return "text".equals(body.get("msg_type"))
                    && body.containsKey("timestamp")
                    && body.containsKey("sign")
                    && text instanceof String message
                    && message.contains("【官网预约演示】")
                    && message.contains("联系人：张三")
                    && message.contains("公司名称：上海零售有限公司")
                    && message.contains("手机号：13800138000")
                    && message.contains("预约ID：88");
        }), eq(String.class));
    }

    private Lead sampleLead() {
        Lead lead = new Lead();
        lead.setId(88L);
        lead.setContactName("张三");
        lead.setCompanyName("上海零售有限公司");
        lead.setPhone("13800138000");
        lead.setDemand("需要演示排班考勤");
        lead.setSourcePage("contact");
        lead.setCreatedAt(LocalDateTime.of(2026, 6, 17, 8, 30));
        return lead;
    }
}
