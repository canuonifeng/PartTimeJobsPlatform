package com.parttime.enterprise.controller;

import com.parttime.enterprise.config.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerTest {

    private MockMvc mockMvc;

    private static final String SECRET = "parttime-enterprise-jwt-secret-key-must-be-at-least-256-bits";
    private static final long EXPIRATION = 86400000L;

    @BeforeEach
    void setUp() {
        JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(SECRET, EXPIRATION);

        var userDetails = User.withUsername("admin")
                .password(new BCryptPasswordEncoder().encode("admin123"))
                .roles("USER")
                .build();
        var userDetailsService = new InMemoryUserDetailsManager(userDetails);
        var authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(new BCryptPasswordEncoder());
        var authManager = new ProviderManager(authProvider);

        AuthController authController = new AuthController();
        ReflectionTestUtils.setField(authController, "jwtTokenProvider", jwtTokenProvider);
        ReflectionTestUtils.setField(authController, "authenticationManager", authManager);

        mockMvc = MockMvcBuilders
                .standaloneSetup(authController)
                .build();
    }

    @Test
    void login_withValidCredentials_shouldReturn200WithToken() throws Exception {
        mockMvc.perform(post("/api/enterprise/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"admin123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").isNotEmpty());
    }

    @Test
    void login_withInvalidCredentials_shouldReturn401() throws Exception {
        mockMvc.perform(post("/api/enterprise/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"wrong\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("未登录"));
    }
}
