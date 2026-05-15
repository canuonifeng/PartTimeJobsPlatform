package com.parttime.cservice.web.controller;

import com.parttime.cservice.core.dto.ProfileResponse;
import com.parttime.cservice.core.dto.ProfileUpdateRequest;
import com.parttime.cservice.core.dto.ResumeResponse;
import com.parttime.cservice.core.dto.ResumeUploadRequest;
import com.parttime.cservice.core.service.ProfileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    private Long getCurrentWorkerId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        return Long.valueOf(auth.getName());
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile() {
        Long workerId = getCurrentWorkerId();
        if (workerId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            ProfileResponse response = profileService.getProfile(workerId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody ProfileUpdateRequest request) {
        Long workerId = getCurrentWorkerId();
        if (workerId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        ProfileResponse response = profileService.updateProfile(workerId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/profile/resumes")
    public ResponseEntity<?> uploadResume(@RequestBody ResumeUploadRequest request) {
        Long workerId = getCurrentWorkerId();
        if (workerId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        ResumeResponse response = profileService.uploadResume(workerId, request.getFileName(), request.getFileUrl());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/profile/resumes")
    public ResponseEntity<?> getResumes() {
        Long workerId = getCurrentWorkerId();
        if (workerId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<ResumeResponse> responses = profileService.getResumes(workerId);
        return ResponseEntity.ok(responses);
    }
}
