package com.parttime.platform.service.notification;

import com.parttime.platform.config.FeishuRobotProperties;
import com.parttime.platform.pojo.entity.Lead;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class FeishuRobotLeadNotifier implements LeadNotifier {

    private static final Logger log = LoggerFactory.getLogger(FeishuRobotLeadNotifier.class);
    private static final String HMAC_SHA256 = "HmacSHA256";

    private final FeishuRobotProperties properties;
    private final RestTemplate restTemplate;

    @Autowired
    public FeishuRobotLeadNotifier(FeishuRobotProperties properties) {
        this(properties, new RestTemplate());
    }

    FeishuRobotLeadNotifier(FeishuRobotProperties properties, RestTemplate restTemplate) {
        this.properties = properties;
        this.restTemplate = restTemplate;
    }

    @Override
    public void notifyLeadCreated(Lead lead) {
        if (!properties.isEnabled() || !StringUtils.hasText(properties.getWebhookUrl())) {
            return;
        }

        try {
            Map<String, Object> body = buildRequestBody(lead);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            restTemplate.postForEntity(properties.getWebhookUrl(), new HttpEntity<>(body, headers), String.class);
        } catch (Exception ex) {
            log.warn("Failed to send Feishu lead notification, leadId={}", lead.getId(), ex);
        }
    }

    private Map<String, Object> buildRequestBody(Lead lead) throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        if (StringUtils.hasText(properties.getSecret())) {
            long timestamp = System.currentTimeMillis() / 1000;
            body.put("timestamp", String.valueOf(timestamp));
            body.put("sign", sign(timestamp, properties.getSecret()));
        }
        body.put("msg_type", "text");
        body.put("content", Map.of("text", buildMessage(lead)));
        return body;
    }

    private String buildMessage(Lead lead) {
        StringBuilder message = new StringBuilder();
        message.append("【官网预约演示】\n");
        message.append("联系人：").append(blankToDash(lead.getContactName())).append('\n');
        message.append("公司名称：").append(blankToDash(lead.getCompanyName())).append('\n');
        message.append("手机号：").append(blankToDash(lead.getPhone())).append('\n');
        message.append("需求说明：").append(blankToDash(lead.getDemand())).append('\n');
        message.append("来源页面：").append(blankToDash(lead.getSourcePage())).append('\n');
        message.append("预约ID：").append(lead.getId() == null ? "-" : lead.getId()).append('\n');
        message.append("创建时间：").append(lead.getCreatedAt() == null ? LocalDateTime.now() : lead.getCreatedAt());
        return message.toString();
    }

    private String sign(long timestamp, String secret) throws Exception {
        String stringToSign = timestamp + "\n" + secret;
        Mac mac = Mac.getInstance(HMAC_SHA256);
        mac.init(new SecretKeySpec(stringToSign.getBytes(StandardCharsets.UTF_8), HMAC_SHA256));
        return Base64.getEncoder().encodeToString(mac.doFinal(new byte[]{}));
    }

    private String blankToDash(String value) {
        return StringUtils.hasText(value) ? value : "-";
    }
}
