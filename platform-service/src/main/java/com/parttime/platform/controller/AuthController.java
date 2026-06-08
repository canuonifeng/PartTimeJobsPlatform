package com.parttime.platform.controller;

import com.parttime.platform.config.JwtTokenProvider;
import com.parttime.platform.pojo.cmd.LoginCmd;
import com.parttime.platform.pojo.vo.ApiResponse;
import com.parttime.platform.pojo.vo.LoginVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Resource
    private JwtTokenProvider jwtTokenProvider;
    @Resource
    private AuthenticationManager authenticationManager;

    @Operation(summary = "平台端登录", description = "平台管理员通过用户名密码登录，返回JWT令牌")
    @PostMapping("/login")
    public ApiResponse<LoginVO> login(@Parameter(description = "登录请求") @RequestBody LoginCmd request) {
        try {
            var auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password()));
            List<String> roles = auth.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .toList();
            String token = jwtTokenProvider.generateToken(auth.getName(), roles);
            return ApiResponse.success(new LoginVO(token));
        } catch (BadCredentialsException e) {
            return ApiResponse.error(401, "未登录");
        }
    }
}
