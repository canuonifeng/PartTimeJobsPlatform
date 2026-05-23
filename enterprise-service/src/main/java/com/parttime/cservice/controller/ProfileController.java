package com.parttime.cservice.controller;

import com.parttime.cservice.pojo.vo.ProfileVO;
import com.parttime.cservice.pojo.cmd.ProfileUpdateCmd;
import com.parttime.cservice.pojo.vo.ResumeVO;
import com.parttime.cservice.pojo.cmd.ResumeUploadCmd;
import com.parttime.cservice.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/worker")
public class ProfileController {

    @Resource
    private ProfileService profileService;

    private Long getCurrentWorkerId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        return Long.valueOf(auth.getName());
    }

    @Operation(summary = "获取工人档案", description = "获取当前登录工人的详细档案信息")
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile() {
        Long workerId = getCurrentWorkerId();
        if (workerId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            ProfileVO response = profileService.getProfile(workerId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "更新工人档案", description = "更新当前登录工人的档案信息")
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody ProfileUpdateCmd request) {
        Long workerId = getCurrentWorkerId();
        if (workerId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        ProfileVO response = profileService.updateProfile(workerId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "上传简历", description = "工人上传简历文件")
    @PostMapping("/profile/resumes")
    public ResponseEntity<?> uploadResume(@RequestBody ResumeUploadCmd request) {
        Long workerId = getCurrentWorkerId();
        if (workerId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        ResumeVO response = profileService.uploadResume(workerId, request.getFileName(), request.getFileUrl());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "获取简历列表", description = "获取当前登录工人的简历列表")
    @GetMapping("/profile/resumes")
    public ResponseEntity<?> getResumes() {
        Long workerId = getCurrentWorkerId();
        if (workerId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<ResumeVO> responses = profileService.getResumes(workerId);
        return ResponseEntity.ok(responses);
    }
}
