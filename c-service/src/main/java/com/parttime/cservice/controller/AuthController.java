package com.parttime.cservice.controller;

import com.parttime.cservice.config.JwtTokenProvider;
import com.parttime.cservice.pojo.cmd.WeChatLoginCmd;
import com.parttime.cservice.pojo.vo.LoginVO;
import com.parttime.cservice.pojo.cmd.LoginCmd;
import com.parttime.cservice.pojo.vo.LoginVO;
import com.parttime.cservice.pojo.cmd.RegisterCmd;
import com.parttime.cservice.pojo.vo.WorkerVO;
import com.parttime.cservice.service.WorkerService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Resource
    private WorkerService workerService;
    @Resource
    private JwtTokenProvider jwtTokenProvider;

    @PostMapping("/register")
    public ResponseEntity<LoginVO> register(@RequestBody RegisterCmd request) {
        WorkerVO worker = workerService.register(request);
        String token = jwtTokenProvider.generateToken(String.valueOf(worker.getId()), List.of("ROLE_WORKER"));
        return ResponseEntity.ok(new LoginVO(token, worker.getId()));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginVO> login(@RequestBody LoginCmd request) {
        String token = workerService.login(request.wechatCode());
        String userId = jwtTokenProvider.getUserIdFromToken(token);
        return ResponseEntity.ok(new LoginVO(token, Long.valueOf(userId)));
    }

    @PostMapping("/wechat-login")
    public ResponseEntity<LoginVO> wechatLogin(@RequestBody WeChatLoginCmd request) {
        LoginVO response = workerService.loginWithWechat(request.code());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/profile")
    public ResponseEntity<WorkerVO> profile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long workerId = Long.valueOf(authentication.getName());
        WorkerVO worker = workerService.getWorkerById(workerId);
        return ResponseEntity.ok(worker);
    }

    @PutMapping("/profile")
    public ResponseEntity<WorkerVO> updateProfile(@RequestBody RegisterCmd request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long workerId = Long.valueOf(authentication.getName());
        WorkerVO worker = workerService.updateProfile(workerId, request);
        return ResponseEntity.ok(worker);
    }
}
