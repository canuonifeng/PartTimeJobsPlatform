package com.parttime.cservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "wechat")
public class WeChatConfig {

    private String appId = "mock_app_id";
    private String appSecret = "mock_app_secret";
    private String loginUrl = "https://api.weixin.qq.com/sns/jscode2session";
    private String accessTokenUrl = "https://api.weixin.qq.com/cgi-bin/token";
    private String phoneNumberUrl = "https://api.weixin.qq.com/wxa/business/getuserphonenumber";

    public String getAppId() { return appId; }
    public void setAppId(String appId) { this.appId = appId; }
    public String getAppSecret() { return appSecret; }
    public void setAppSecret(String appSecret) { this.appSecret = appSecret; }
    public String getLoginUrl() { return loginUrl; }
    public void setLoginUrl(String loginUrl) { this.loginUrl = loginUrl; }
    public String getAccessTokenUrl() { return accessTokenUrl; }
    public void setAccessTokenUrl(String accessTokenUrl) { this.accessTokenUrl = accessTokenUrl; }
    public String getPhoneNumberUrl() { return phoneNumberUrl; }
    public void setPhoneNumberUrl(String phoneNumberUrl) { this.phoneNumberUrl = phoneNumberUrl; }
}
