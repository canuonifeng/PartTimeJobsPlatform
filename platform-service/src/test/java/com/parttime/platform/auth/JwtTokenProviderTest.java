package com.parttime.platform.auth;

import com.parttime.platform.config.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    private static final String SECRET = "parttime-platform-jwt-secret-key-must-be-at-least-256-bits-long-enough";
    private static final long EXPIRATION = 86400000L;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(SECRET, EXPIRATION);
    }

    @Test
    void generateToken_shouldReturnNonNullToken() {
        String token = jwtTokenProvider.generateToken("admin", List.of("ROLE_ADMIN"));
        assertThat(token).isNotNull();
    }

    @Test
    void generateToken_shouldReturnValidJwt() {
        String token = jwtTokenProvider.generateToken("admin", List.of("ROLE_ADMIN"));
        assertThat(token).isNotBlank();
    }

    @Test
    void validateToken_withValidToken_shouldReturnTrue() {
        String token = jwtTokenProvider.generateToken("admin", List.of("ROLE_ADMIN"));
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
    }

    @Test
    void validateToken_withInvalidToken_shouldReturnFalse() {
        assertThat(jwtTokenProvider.validateToken("invalid.token.here")).isFalse();
    }

    @Test
    void getUserIdFromToken_shouldReturnCorrectUserId() {
        String token = jwtTokenProvider.generateToken("admin", List.of("ROLE_ADMIN"));
        String userId = jwtTokenProvider.getUserIdFromToken(token);
        assertThat(userId).isEqualTo("admin");
    }

    @Test
    void getRolesFromToken_shouldReturnCorrectRoles() {
        String token = jwtTokenProvider.generateToken("admin", List.of("ROLE_ADMIN"));
        List<String> roles = jwtTokenProvider.getRolesFromToken(token);
        assertThat(roles).containsExactly("ROLE_ADMIN");
    }
}
