package com.parttime.enterprise.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    private static final String SECRET = "parttime-enterprise-jwt-secret-key-must-be-at-least-256-bits";
    private static final long EXPIRATION = 86400000L;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(SECRET, EXPIRATION);
    }

    @Test
    void generateToken_shouldReturnNonNullToken() {
        String token = jwtTokenProvider.generateToken("user123", List.of("ROLE_USER"));
        assertThat(token).isNotNull();
    }

    @Test
    void generateToken_shouldReturnValidJwt() {
        String token = jwtTokenProvider.generateToken("user123", List.of("ROLE_USER"));
        assertThat(token).isNotBlank();
    }

    @Test
    void validateToken_withValidToken_shouldReturnTrue() {
        String token = jwtTokenProvider.generateToken("user123", List.of("ROLE_USER"));
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
    }

    @Test
    void validateToken_withInvalidToken_shouldReturnFalse() {
        assertThat(jwtTokenProvider.validateToken("invalid.token.here")).isFalse();
    }

    @Test
    void getUserIdFromToken_shouldReturnCorrectUserId() {
        String token = jwtTokenProvider.generateToken("user123", List.of("ROLE_USER"));
        String userId = jwtTokenProvider.getUserIdFromToken(token);
        assertThat(userId).isEqualTo("user123");
    }

    @Test
    void getRolesFromToken_shouldReturnCorrectRoles() {
        String token = jwtTokenProvider.generateToken("user123", List.of("ROLE_USER", "ROLE_ADMIN"));
        List<String> roles = jwtTokenProvider.getRolesFromToken(token);
        assertThat(roles).containsExactly("ROLE_USER", "ROLE_ADMIN");
    }
}
