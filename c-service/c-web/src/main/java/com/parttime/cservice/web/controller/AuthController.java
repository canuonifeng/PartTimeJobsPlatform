package com.parttime.cservice.web.controller;

import com.parttime.cservice.core.auth.JwtTokenProvider;
import com.parttime.cservice.core.dto.WorkerLoginRequest;
import com.parttime.cservice.core.dto.WorkerLoginResponse;
import com.parttime.cservice.core.dto.WorkerRegisterRequest;
import com.parttime.cservice.core.dto.WorkerResponse;
import com.parttime.cservice.core.service.WorkerService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final WorkerService workerService;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthController(WorkerService workerService, JwtTokenProvider jwtTokenProvider) {
        this.workerService = workerService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/register")
    public ResponseEntity<WorkerLoginResponse> register(@RequestBody WorkerRegisterRequest request) {
        WorkerResponse worker = workerService.register(request);
        String token = jwtTokenProvider.generateToken(String.valueOf(worker.getId()), List.of("ROLE_WORKER"));
        return ResponseEntity.ok(new WorkerLoginResponse(token, worker.getId()));
    }

    @PostMapping("/login")
    public ResponseEntity<WorkerLoginResponse> login(@RequestBody WorkerLoginRequest request) {
        String token = workerService.login(request.wechatCode());
        String userId = jwtTokenProvider.getUserIdFromToken(token);
        return ResponseEntity.ok(new WorkerLoginResponse(token, Long.valueOf(userId)));
    }

    @GetMapping("/profile")
    public ResponseEntity<WorkerResponse> profile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long workerId = Long.valueOf(authentication.getName());
        WorkerResponse worker = workerService.getWorkerById(workerId);
        return ResponseEntity.ok(worker);
    }

    @PutMapping("/profile")
    public ResponseEntity<WorkerResponse> updateProfile(@RequestBody WorkerRegisterRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long workerId = Long.valueOf(authentication.getName());
        WorkerResponse worker = workerService.updateProfile(workerId, request);
        return ResponseEntity.ok(worker);
    }
}
