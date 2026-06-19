package com.parttime.cservice.service;

import com.parttime.cservice.config.WeChatConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
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

    private static final Logger log = LoggerFactory.getLogger(WeChatSchemeService.class);

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
                "query", query,
                "env_version", "release"
        );
        Map<String, Object> body = Map.of(
                "jump_wxa", jumpWxa,
                "expire_type", 1,
                "expire_interval", 30
        );
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            Map<?, ?> result = response.getBody();
            if (result == null || result.get("errcode") != null && !result.get("errcode").equals(0)) {
                String errMsg = result != null ? String.valueOf(result.get("errmsg")) : "无响应";
                log.warn("微信Scheme生成失败: errcode={}, errmsg={}", result != null ? result.get("errcode") : "null", errMsg);
                throw new RuntimeException("微信 URL Scheme 生成失败: " + errMsg);
            }
            return String.valueOf(result.get("openlink"));
        } catch (Exception e) {
            log.error("调用微信Scheme接口异常: {}", e.getMessage(), e);
            throw new RuntimeException("微信 URL Scheme 生成失败: " + e.getMessage(), e);
        }
    }

    private String getAccessToken() {
        if (Instant.now().isBefore(tokenExpiry) && cachedToken != null) {
            return cachedToken;
        }
        String url = weChatConfig.getAccessTokenUrl()
                + "?grant_type=client_credential&appid=" + weChatConfig.getAppId()
                + "&secret=" + weChatConfig.getAppSecret();
        try {
            Map<?, ?> response = restTemplate.getForObject(url, Map.class);
            if (response == null || response.get("access_token") == null) {
                String errMsg = response != null ? String.valueOf(response.get("errmsg")) : "无响应";
                log.error("微信access_token获取失败: {}", errMsg);
                throw new RuntimeException("微信 access_token 获取失败: " + errMsg);
            }
            cachedToken = String.valueOf(response.get("access_token"));
            Object expiresIn = response.get("expires_in");
            tokenExpiry = Instant.now().plusSeconds(expiresIn instanceof Number n ? n.intValue() - 60 : 7100);
            log.info("微信access_token获取成功, 过期时间: {}", tokenExpiry);
            return cachedToken;
        } catch (Exception e) {
            log.error("调用微信access_token接口异常: {}", e.getMessage(), e);
            throw new RuntimeException("微信 access_token 获取失败: " + e.getMessage(), e);
        }
    }

    public String generateUrlLink(String path, String query) {
        if (isMockConfig()) {
            return "https://wxaurl.cn/mock_" + query;
        }
        String token = getAccessToken();
        String url = "https://api.weixin.qq.com/wxa/generate_urllink?access_token=" + token;
        Map<String, Object> body = Map.of(
                "path", path,
                "query", query,
                "is_expire", true,
                "expire_type", 1,
                "expire_interval", 30,
                "env_version", "release"
        );
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            Map<?, ?> result = response.getBody();
            if (result == null || result.get("errcode") != null && !result.get("errcode").equals(0)) {
                String errMsg = result != null ? String.valueOf(result.get("errmsg")) : "无响应";
                log.warn("微信UrlLink生成失败: errcode={}, errmsg={}", result != null ? result.get("errcode") : "null", errMsg);
                throw new RuntimeException("微信 URL Link 生成失败: " + errMsg);
            }
            return String.valueOf(result.get("url_link"));
        } catch (Exception e) {
            log.error("调用微信UrlLink接口异常: {}", e.getMessage(), e);
            throw new RuntimeException("微信 URL Link 生成失败: " + e.getMessage(), e);
        }
    }

    private boolean isMockConfig() {
        return weChatConfig.getAppId() == null || weChatConfig.getAppId().startsWith("mock_");
    }
}
