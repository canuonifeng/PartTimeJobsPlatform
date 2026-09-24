package com.parttime.platform.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "oss")
public class OssProperties {

    private String endpoint;
    private String region;
    private String accessKeyId;
    private String accessKeySecret;
    private String privateBucket;
    private String publicBucket;
    private String stsRoleArn;
    private String stsRoleSessionName;
}
