package com.parttime.cservice.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityConfigTest {

    @Test
    void corsConfiguration_shouldAllowLocalOriginsWithAnyPort() {
        SecurityConfig config = new SecurityConfig(new JwtTokenProvider("parttime-cservice-jwt-secret-key-must-be-at-least-256-bits", 86400000));
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/worker/profile");

        CorsConfiguration cors = config.corsConfigurationSource().getCorsConfiguration(request);

        assertThat(cors).isNotNull();
        assertThat(cors.checkOrigin("http://localhost:5173")).isEqualTo("http://localhost:5173");
        assertThat(cors.checkOrigin("http://127.0.0.1:5173")).isEqualTo("http://127.0.0.1:5173");
        assertThat(cors.getAllowedMethods()).contains("OPTIONS", "GET", "POST", "PUT", "DELETE");
        assertThat(cors.getAllowedHeaders()).contains("Authorization", "Content-Type");
    }
}
