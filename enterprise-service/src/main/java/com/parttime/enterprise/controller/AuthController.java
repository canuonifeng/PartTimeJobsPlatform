package com.parttime.enterprise.controller;

import com.parttime.enterprise.config.JwtTokenProvider;
import com.parttime.enterprise.pojo.cmd.LoginCmd;
import com.parttime.enterprise.pojo.vo.LoginVO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/api/auth")
public class AuthController {

    @Resource
    private JwtTokenProvider jwtTokenProvider;
    @Resource
    private AuthenticationManager authenticationManager;

    @PostMapping("/login")
    public ResponseEntity<LoginVO> login(@RequestBody LoginCmd request) {
        try {
            var auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password()));
            List<String> roles = auth.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .toList();
            String token = jwtTokenProvider.generateToken(auth.getName(), roles);
            return ResponseEntity.ok(new LoginVO(token));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
