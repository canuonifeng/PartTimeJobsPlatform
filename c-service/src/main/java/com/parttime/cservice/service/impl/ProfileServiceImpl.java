package com.parttime.cservice.service.impl;

import com.parttime.cservice.pojo.vo.ProfileVO;
import com.parttime.cservice.pojo.cmd.ProfileUpdateCmd;
import com.parttime.cservice.pojo.vo.ResumeVO;
import com.parttime.cservice.pojo.entity.WorkerProfile;
import com.parttime.cservice.pojo.entity.WorkerResume;
import com.parttime.cservice.service.ProfileService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class ProfileServiceImpl implements ProfileService {

    private final ConcurrentHashMap<Long, WorkerProfile> profiles = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, List<WorkerResume>> resumes = new ConcurrentHashMap<>();
    private final AtomicLong resumeIdCounter = new AtomicLong(1);

    public ProfileVO updateProfile(Long workerId, ProfileUpdateCmd request) {
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

    public ProfileVO getProfile(Long workerId) {
        WorkerProfile profile = profiles.get(workerId);
        if (profile == null) {
            throw new RuntimeException("Profile not found for worker: " + workerId);
        }
        return toProfileResponse(profile);
    }

    public ResumeVO uploadResume(Long workerId, String fileName, String fileUrl) {
        WorkerResume resume = new WorkerResume(resumeIdCounter.getAndIncrement(), workerId, fileName, fileUrl, LocalDateTime.now());
        resumes.computeIfAbsent(workerId, k -> List.of());
        resumes.compute(workerId, (k, list) -> {
            List<WorkerResume> newList = new java.util.ArrayList<>(list);
            newList.add(resume);
            return newList;
        });
        return toResumeResponse(resume);
    }

    public List<ResumeVO> getResumes(Long workerId) {
        return resumes.getOrDefault(workerId, List.of()).stream()
                .map(this::toResumeResponse)
                .collect(Collectors.toList());
    }

    private ProfileVO toProfileResponse(WorkerProfile profile) {
        ProfileVO response = new ProfileVO();
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

    private ResumeVO toResumeResponse(WorkerResume resume) {
        ResumeVO response = new ResumeVO();
        response.setId(resume.getId());
        response.setWorkerId(resume.getWorkerId());
        response.setFileName(resume.getFileName());
        response.setFileUrl(resume.getFileUrl());
        response.setUploadedAt(resume.getUploadedAt());
        return response;
    }
}
