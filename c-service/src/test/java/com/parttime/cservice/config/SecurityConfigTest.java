package com.parttime.cservice.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityConfigTest {

    @Test
    void corsConfiguration_shouldAllowServerIpWithAnyPort() {
        SecurityConfig config = new SecurityConfig(new JwtTokenProvider("parttime-cservice-jwt-secret-key-must-be-at-least-256-bits", 86400000));
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/profile");

        CorsConfiguration cors = config.corsConfigurationSource().getCorsConfiguration(request);

        assertThat(cors).isNotNull();
        assertThat(cors.checkOrigin("http://121.199.12.23:5173")).isEqualTo("http://121.199.12.23:5173");
        assertThat(cors.getAllowedMethods()).contains("OPTIONS", "GET", "POST", "PUT", "DELETE");
        assertThat(cors.getAllowedHeaders()).contains("Authorization", "Content-Type");
    }
}
