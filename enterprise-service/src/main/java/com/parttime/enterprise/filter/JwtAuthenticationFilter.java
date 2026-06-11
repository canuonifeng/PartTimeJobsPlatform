package com.parttime.enterprise.filter;

import com.parttime.enterprise.config.JwtTokenProvider;
import com.parttime.enterprise.mapper.EnterpriseAccountMapper;
import com.parttime.enterprise.pojo.entity.EnterpriseAccount;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final EnterpriseAccountMapper accountMapper;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, EnterpriseAccountMapper accountMapper) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.accountMapper = accountMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = resolveToken(request);

        if (token != null && jwtTokenProvider.validateToken(token)) {
            String userId = jwtTokenProvider.getUserIdFromToken(token);
            List<String> roles = jwtTokenProvider.getRolesFromToken(token);
            Long companyId = jwtTokenProvider.getCompanyIdFromToken(token);
            EnterpriseAccount account = accountMapper.findByUsername(userId).orElse(null);
            if (account == null || !"ACTIVE".equals(account.getStatus()) || !Objects.equals(account.getEnterpriseId(), companyId)) {
                filterChain.doFilter(request, response);
                return;
            }

            List<SimpleGrantedAuthority> authorities = roles.stream()
                    .map(SimpleGrantedAuthority::new)
                    .toList();

            Map<String, Object> details = new HashMap<>();
            details.put("companyId", companyId);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userId, null, authorities);
            authentication.setDetails(details);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
