package com.parttime.enterprise.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "wechat")
public class WeChatMiniProgramConfig {

    private String workerAppId = "mock_worker_app_id";
    private String workerAppSecret = "mock_worker_app_secret";
    private String customerAppId = "wx3fdcd5b95534d8f8";
    private String tokenUrl = "https://api.weixin.qq.com/cgi-bin/token";
    private String codeUrl = "https://api.weixin.qq.com/wxa/getwxacodeunlimit";

    public String getWorkerAppId() {
        return workerAppId;
    }

    public void setWorkerAppId(String workerAppId) {
        this.workerAppId = workerAppId;
    }

    public String getWorkerAppSecret() {
        return workerAppSecret;
    }

    public void setWorkerAppSecret(String workerAppSecret) {
        this.workerAppSecret = workerAppSecret;
    }

    public String getCustomerAppId() {
        return customerAppId;
    }

    public void setCustomerAppId(String customerAppId) {
        this.customerAppId = customerAppId;
    }

    public String getTokenUrl() {
        return tokenUrl;
    }

    public void setTokenUrl(String tokenUrl) {
        this.tokenUrl = tokenUrl;
    }

    public String getCodeUrl() {
        return codeUrl;
    }

    public void setCodeUrl(String codeUrl) {
        this.codeUrl = codeUrl;
    }
}
