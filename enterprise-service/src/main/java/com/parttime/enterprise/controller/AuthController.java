package com.parttime.enterprise.controller;

import com.parttime.enterprise.config.CompanyUserDetails;
import com.parttime.enterprise.config.JwtTokenProvider;
import com.parttime.enterprise.pojo.cmd.LoginCmd;
import com.parttime.enterprise.pojo.vo.LoginVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import com.parttime.enterprise.pojo.vo.ApiResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

import java.util.List;

@RestController
@RequestMapping("/api/enterprise/auth")
public class AuthController {

    @Resource
    private JwtTokenProvider jwtTokenProvider;
    @Resource
    private AuthenticationManager authenticationManager;

    @Operation(summary = "企业端登录", description = "企业用户通过用户名密码登录，返回JWT令牌")
    @PostMapping("/login")
    public ApiResponse<LoginVO> login(@Parameter(description = "登录请求") @RequestBody LoginCmd request) {
        try {
            var auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password()));
            List<String> roles = auth.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .toList();
            Long companyId = 1L;
            if (auth.getPrincipal() instanceof CompanyUserDetails details) {
                companyId = details.getCompanyId();
            }
            String token = jwtTokenProvider.generateToken(auth.getName(), roles, companyId);
            return ApiResponse.success(new LoginVO(token));
        } catch (BadCredentialsException e) {
            return ApiResponse.error(401, "未登录");
        }
    }
}
