package com.parttime.enterprise.config;

import com.parttime.enterprise.service.EnterpriseUserDetailsService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class MergedSecurityConfig {

    @Resource
    private EnterpriseUserDetailsService enterpriseUserDetailsService;

    // ============ JwtTokenProvider beans ============

    @Bean
    public com.parttime.enterprise.config.JwtTokenProvider enterpriseJwtTokenProvider(
            @Value("${jwt.enterprise.secret}") String secret,
            @Value("${jwt.enterprise.expiration}") long expiration) {
        return new com.parttime.enterprise.config.JwtTokenProvider(secret, expiration);
    }

    @Bean
    public com.parttime.platform.config.JwtTokenProvider platformJwtTokenProvider(
            @Value("${jwt.platform.secret}") String secret,
            @Value("${jwt.platform.expiration}") long expiration) {
        return new com.parttime.platform.config.JwtTokenProvider(secret, expiration);
    }

    @Bean
    public com.parttime.cservice.config.JwtTokenProvider workerJwtTokenProvider(
            @Value("${jwt.worker.secret}") String secret,
            @Value("${jwt.worker.expiration}") long expiration) {
        return new com.parttime.cservice.config.JwtTokenProvider(secret, expiration);
    }

    // ============ Enterprise Security Filter Chain ============

    @Bean
    @Order(1)
    public SecurityFilterChain enterpriseSecurityFilterChain(HttpSecurity http,
            com.parttime.enterprise.config.JwtTokenProvider enterpriseJwtTokenProvider) throws Exception {
        http.securityMatcher("/api/enterprise/**")
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(new AntPathRequestMatcher("/api/enterprise/auth/**")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/api/enterprise/**")).authenticated()
                        .anyRequest().permitAll()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) ->
                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED))
                )
                .addFilterBefore(
                        new com.parttime.enterprise.filter.JwtAuthenticationFilter(enterpriseJwtTokenProvider),
                        UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    // ============ Platform Security Filter Chain ============

    @Bean
    @Order(2)
    public SecurityFilterChain platformSecurityFilterChain(HttpSecurity http,
            com.parttime.platform.config.JwtTokenProvider platformJwtTokenProvider) throws Exception {
        http.securityMatcher("/api/platform/**")
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(new AntPathRequestMatcher("/api/platform/auth/**")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/api/platform/**")).authenticated()
                        .anyRequest().permitAll()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) ->
                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED))
                )
                .addFilterBefore(
                        new com.parttime.platform.filter.JwtAuthenticationFilter(platformJwtTokenProvider),
                        UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    // ============ Worker Security Filter Chain ============

    @Bean
    @Order(3)
    public SecurityFilterChain workerSecurityFilterChain(HttpSecurity http,
            com.parttime.cservice.config.JwtTokenProvider workerJwtTokenProvider) throws Exception {
        http.securityMatcher("/api/worker/**")
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(new AntPathRequestMatcher("/api/worker/auth/**")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/api/worker/jobs/**")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/api/worker/**")).authenticated()
                        .anyRequest().permitAll()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) ->
                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED))
                )
                .addFilterBefore(
                        new com.parttime.cservice.filter.JwtAuthenticationFilter(workerJwtTokenProvider),
                        UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    // ============ Shared beans ============

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(PasswordEncoder encoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(enterpriseUserDetailsService);
        provider.setPasswordEncoder(encoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationProvider provider) {
        return new ProviderManager(provider);
    }

    @Bean
    static MethodSecurityExpressionHandler methodSecurityExpressionHandler(RoleHierarchy roleHierarchy) {
        var handler = new DefaultMethodSecurityExpressionHandler();
        handler.setRoleHierarchy(roleHierarchy);
        return handler;
    }

    @Bean
    public RoleHierarchy roleHierarchy() {
        var hierarchy = new RoleHierarchyImpl();
        hierarchy.setHierarchy("ROLE_ADMIN > ROLE_HR\nROLE_ADMIN > ROLE_MANAGER\nROLE_ADMIN > ROLE_FINANCE");
        return hierarchy;
    }

    @Bean
    public UserDetailsService enterpriseUserDetailsServiceBean() {
        return enterpriseUserDetailsService;
    }

    @Bean
    public UserDetailsService platformUserDetailsService(PasswordEncoder passwordEncoder) {
        var admin = User.withUsername("admin")
                .password(passwordEncoder.encode("admin123"))
                .roles("ADMIN")
                .build();
        return new InMemoryUserDetailsManager(admin);
    }
}
