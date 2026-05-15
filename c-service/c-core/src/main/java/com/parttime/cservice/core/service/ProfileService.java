package com.parttime.cservice.core.service;

import com.parttime.cservice.core.dto.ProfileResponse;
import com.parttime.cservice.core.dto.ProfileUpdateRequest;
import com.parttime.cservice.core.dto.ResumeResponse;
import com.parttime.cservice.core.model.WorkerProfile;
import com.parttime.cservice.core.model.WorkerResume;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class ProfileService {

    private final ConcurrentHashMap<Long, WorkerProfile> profiles = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, List<WorkerResume>> resumes = new ConcurrentHashMap<>();
    private final AtomicLong resumeIdCounter = new AtomicLong(1);

    public ProfileResponse updateProfile(Long workerId, ProfileUpdateRequest request) {
        WorkerProfile profile = profiles.computeIfAbsent(workerId, id -> {
            WorkerProfile p = new WorkerProfile(workerId, null, null, null);
            p.setCreatedAt(LocalDateTime.now());
            return p;
        });
        if (request.getName() != null) profile.setName(request.getName());
        if (request.getPhone() != null) profile.setPhone(request.getPhone());
        if (request.getAvatarUrl() != null) profile.setAvatarUrl(request.getAvatarUrl());
        if (request.getSkills() != null) profile.setSkills(request.getSkills());
        if (request.getAvailableDays() != null) profile.setAvailableDays(request.getAvailableDays());
        profile.setUpdatedAt(LocalDateTime.now());
        return toProfileResponse(profile);
    }

    public ProfileResponse getProfile(Long workerId) {
        WorkerProfile profile = profiles.get(workerId);
        if (profile == null) {
            throw new RuntimeException("Profile not found for worker: " + workerId);
        }
        return toProfileResponse(profile);
    }

    public ResumeResponse uploadResume(Long workerId, String fileName, String fileUrl) {
        WorkerResume resume = new WorkerResume(resumeIdCounter.getAndIncrement(), workerId, fileName, fileUrl, LocalDateTime.now());
        resumes.computeIfAbsent(workerId, k -> List.of());
        resumes.compute(workerId, (k, list) -> {
            List<WorkerResume> newList = new java.util.ArrayList<>(list);
            newList.add(resume);
            return newList;
        });
        return toResumeResponse(resume);
    }

    public List<ResumeResponse> getResumes(Long workerId) {
        return resumes.getOrDefault(workerId, List.of()).stream()
                .map(this::toResumeResponse)
                .collect(Collectors.toList());
    }

    private ProfileResponse toProfileResponse(WorkerProfile profile) {
        ProfileResponse response = new ProfileResponse();
        response.setWorkerId(profile.getWorkerId());
        response.setName(profile.getName());
        response.setPhone(profile.getPhone());
        response.setAvatarUrl(profile.getAvatarUrl());
        response.setSkills(profile.getSkills());
        response.setAvailableDays(profile.getAvailableDays());
        response.setCreatedAt(profile.getCreatedAt());
        response.setUpdatedAt(profile.getUpdatedAt());
        return response;
    }

    private ResumeResponse toResumeResponse(WorkerResume resume) {
        ResumeResponse response = new ResumeResponse();
        response.setId(resume.getId());
        response.setWorkerId(resume.getWorkerId());
        response.setFileName(resume.getFileName());
        response.setFileUrl(resume.getFileUrl());
        response.setUploadedAt(resume.getUploadedAt());
        return response;
    }
}
