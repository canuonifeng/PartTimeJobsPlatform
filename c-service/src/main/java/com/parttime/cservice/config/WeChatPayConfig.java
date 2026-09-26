package com.parttime.cservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "wechat.pay")
public class WeChatPayConfig {

    private String appId;
    private String mchId;
    private String apiKey;
    private String certPath;
    private String privateKeyPath;
    private String notifyUrl;
    private String apiV3Key;
}