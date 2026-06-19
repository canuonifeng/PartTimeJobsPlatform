package com.parttime.cservice.service;

import com.parttime.cservice.config.WeChatConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

class WeChatSchemeServiceTest {

    @Mock
    private WeChatConfig weChatConfig;
    @Mock
    private RestTemplate restTemplate;
    @InjectMocks
    private WeChatSchemeService schemeService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void generateScheme_withMockConfig_returnsMockScheme() {
        String scheme = schemeService.generateScheme("/pages/register/register", "code=ABC123");

        assertThat(scheme).contains("mock_scheme");
    }

    @Test
    void generateScheme_withRealConfig_callsWeChatApi() {
        when(weChatConfig.getAppId()).thenReturn("wx_real_id");
        when(weChatConfig.getAppSecret()).thenReturn("real_secret");
        when(weChatConfig.getAccessTokenUrl()).thenReturn("https://api.weixin.qq.com/cgi-bin/token");
        when(weChatConfig.getSchemeUrl()).thenReturn("https://api.weixin.qq.com/wxa/generatescheme");
        when(restTemplate.getForObject(contains("grant_type=client_credential"), eq(Map.class)))
                .thenReturn(Map.of("access_token", "test_token", "expires_in", 7200));
        when(restTemplate.postForObject(contains("access_token=test_token"), any(), eq(Map.class)))
                .thenReturn(Map.of("errcode", 0, "openlink", "weixin://dl/business/?t=test"));

        String scheme = schemeService.generateScheme("/pages/register/register", "code=ABC123");

        assertThat(scheme).isEqualTo("weixin://dl/business/?t=test");
    }
}
