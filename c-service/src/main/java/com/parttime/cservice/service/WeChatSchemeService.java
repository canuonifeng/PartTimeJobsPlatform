package com.parttime.cservice.service;

import com.parttime.cservice.config.WeChatConfig;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.Resource;
import java.time.Instant;
import java.util.Map;

@Service
public class WeChatSchemeService {

    @Resource
    private WeChatConfig weChatConfig;

    @Resource
    private RestTemplate restTemplate;

    private String cachedToken;
    private Instant tokenExpiry = Instant.EPOCH;

    public String generateScheme(String path, String query) {
        if (isMockConfig()) {
            return "weixin://dl/business/?t=mock_scheme_" + query;
        }
        String token = getAccessToken();
        String url = weChatConfig.getSchemeUrl() + "?access_token=" + token;
        Map<String, Object> jumpWxa = Map.of(
                "path", path,
                "query", query
        );
        Map<String, Object> body = Map.of(
                "jump_wxa", jumpWxa,
                "expire_type", 1,
                "expire_interval", 30
        );
        Map<?, ?> response = restTemplate.postForObject(url, body, Map.class);
        if (response == null || response.get("errcode") != null && !response.get("errcode").equals(0)) {
            throw new RuntimeException("微信 URL Scheme 生成失败: " + (response != null ? response.get("errmsg") : "无响应"));
        }
        return String.valueOf(response.get("openlink"));
    }

    private String getAccessToken() {
        if (Instant.now().isBefore(tokenExpiry) && cachedToken != null) {
            return cachedToken;
        }
        String url = weChatConfig.getAccessTokenUrl()
                + "?grant_type=client_credential&appid=" + weChatConfig.getAppId()
                + "&secret=" + weChatConfig.getAppSecret();
        Map<?, ?> response = restTemplate.getForObject(url, Map.class);
        if (response == null || response.get("access_token") == null) {
            throw new RuntimeException("微信 access_token 获取失败");
        }
        cachedToken = String.valueOf(response.get("access_token"));
        Object expiresIn = response.get("expires_in");
        tokenExpiry = Instant.now().plusSeconds(expiresIn instanceof Number n ? n.intValue() - 60 : 7100);
        return cachedToken;
    }

    private boolean isMockConfig() {
        return weChatConfig.getAppId() == null || weChatConfig.getAppId().startsWith("mock_");
    }
}
