package com.parttime.cservice.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.profile.DefaultProfile;
import jakarta.annotation.PreDestroy;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnExpression(
        "T(org.springframework.util.StringUtils).hasText('${oss.endpoint:}')"
                + " && T(org.springframework.util.StringUtils).hasText('${oss.access-key-id:}')"
)
public class OssConfig {

    private final OssProperties properties;
    private OSS ossClient;
    private IAcsClient stsClient;

    public OssConfig(OssProperties properties) {
        this.properties = properties;
    }

    @Bean
    public OSS ossClient() {
        this.ossClient = new OSSClientBuilder().build(
                properties.getEndpoint(),
                properties.getAccessKeyId(),
                properties.getAccessKeySecret());
        return this.ossClient;
    }

    @Bean
    public IAcsClient stsClient() {
        DefaultProfile profile = DefaultProfile.getProfile(
                properties.getRegion(),
                properties.getAccessKeyId(),
                properties.getAccessKeySecret());
        this.stsClient = new DefaultAcsClient(profile);
        return this.stsClient;
    }

    @PreDestroy
    public void destroy() {
        if (ossClient != null) {
            ossClient.shutdown();
        }
        if (stsClient != null) {
            stsClient.shutdown();
        }
    }
}
