package com.parttime.platform.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "feishu.robot")
public class FeishuRobotProperties {

    private boolean enabled = false;

    private String webhookUrl;

    private String secret;
}
