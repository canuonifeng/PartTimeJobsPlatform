package com.parttime.cservice.core.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    private static final String SECRET = "parttime-cservice-jwt-secret-key-must-be-at-least-256-bits";
    private static final long EXPIRATION = 86400000L;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(SECRET, EXPIRATION);
    }

    @Test
    void generateToken_shouldReturnNonNullToken() {
        String token = jwtTokenProvider.generateToken("1", List.of("ROLE_WORKER"));
        assertThat(token).isNotNull();
    }

    @Test
    void validateToken_withValidToken_shouldReturnTrue() {
        String token = jwtTokenProvider.generateToken("1", List.of("ROLE_WORKER"));
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
    }

    @Test
    void validateToken_withInvalidToken_shouldReturnFalse() {
        assertThat(jwtTokenProvider.validateToken("invalid.token.here")).isFalse();
    }

    @Test
    void getUserIdFromToken_shouldReturnCorrectUserId() {
        String token = jwtTokenProvider.generateToken("42", List.of("ROLE_WORKER"));
        String userId = jwtTokenProvider.getUserIdFromToken(token);
        assertThat(userId).isEqualTo("42");
    }

    @Test
    void getRolesFromToken_shouldReturnCorrectRoles() {
        String token = jwtTokenProvider.generateToken("1", List.of("ROLE_WORKER"));
        List<String> roles = jwtTokenProvider.getRolesFromToken(token);
        assertThat(roles).containsExactly("ROLE_WORKER");
    }
}
