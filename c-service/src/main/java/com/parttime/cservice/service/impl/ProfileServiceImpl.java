package com.parttime.cservice.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.parttime.cservice.mapper.WorkerProfileMapper;
import com.parttime.cservice.mapper.WorkerResumeMapper;
import com.parttime.cservice.pojo.cmd.ProfileUpdateCmd;
import com.parttime.cservice.pojo.entity.WorkerProfile;
import com.parttime.cservice.pojo.entity.WorkerResume;
import com.parttime.cservice.pojo.vo.ProfileCompletenessVO;
import com.parttime.cservice.pojo.vo.ProfileVO;
import com.parttime.cservice.pojo.vo.ResumeVO;
import com.parttime.cservice.service.ProfileService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProfileServiceImpl implements ProfileService {

    @Resource
    private WorkerProfileMapper workerProfileMapper;
    @Resource
    private WorkerResumeMapper workerResumeMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public ProfileVO updateProfile(Long workerId, ProfileUpdateCmd request) {
        Optional<WorkerProfile> existing = workerProfileMapper.findByWorkerId(workerId);
        WorkerProfile profile;
        if (existing.isPresent()) {
            profile = existing.get();
        } else {
            profile = new WorkerProfile(workerId, null, null, null);
            profile.setCreatedAt(LocalDateTime.now());
        }
        if (request.getName() != null) profile.setName(request.getName());
        if (request.getPhone() != null) profile.setPhone(request.getPhone());
        if (request.getAvatarUrl() != null) profile.setAvatarUrl(request.getAvatarUrl());
        if (request.getSkills() != null) profile.setSkills(toJson(request.getSkills()));
        if (request.getAvailableDays() != null) profile.setAvailableDays(toJson(request.getAvailableDays()));
        if (request.getGender() != null) profile.setGender(request.getGender());
        if (request.getBirthday() != null) profile.setBirthday(request.getBirthday());
        profile.setUpdatedAt(LocalDateTime.now());

        if (existing.isPresent()) {
            workerProfileMapper.update(profile);
        } else {
            workerProfileMapper.insert(profile);
        }
        return toProfileResponse(profile);
    }

    public ProfileVO getProfile(Long workerId) {
        WorkerProfile profile = workerProfileMapper.findByWorkerId(workerId)
                .orElseGet(() -> {
                    WorkerProfile p = new WorkerProfile(workerId, null, null, null);
                    p.setCreatedAt(LocalDateTime.now());
                    p.setUpdatedAt(LocalDateTime.now());
                    workerProfileMapper.insert(p);
                    return p;
                });
        return toProfileResponse(profile);
    }

    public ProfileCompletenessVO getCompleteness(Long workerId) {
        Optional<WorkerProfile> existing = workerProfileMapper.findByWorkerId(workerId);
        List<String> missing = new ArrayList<>();
        if (existing.isEmpty()) {
            missing.add("name");
            missing.add("phone");
            missing.add("gender");
            missing.add("birthday");
            return new ProfileCompletenessVO(false, missing);
        }
        WorkerProfile p = existing.get();
        if (isBlank(p.getName())) missing.add("name");
        if (isBlank(p.getPhone())) missing.add("phone");
        if (isBlank(p.getGender())) missing.add("gender");
        if (p.getBirthday() == null) missing.add("birthday");
        return new ProfileCompletenessVO(missing.isEmpty(), missing);
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    public ResumeVO uploadResume(Long workerId, String fileName, String fileUrl) {
        WorkerResume resume = new WorkerResume();
        resume.setWorkerId(workerId);
        resume.setFileName(fileName);
        resume.setFileUrl(fileUrl);
        resume.setUploadedAt(LocalDateTime.now());
        workerResumeMapper.insert(resume);
        return toResumeResponse(resume);
    }

    public List<ResumeVO> getResumes(Long workerId) {
        return workerResumeMapper.findByWorkerId(workerId).stream()
                .map(this::toResumeResponse)
                .collect(Collectors.toList());
    }

    private ProfileVO toProfileResponse(WorkerProfile profile) {
        ProfileVO response = new ProfileVO();
        response.setWorkerId(profile.getWorkerId());
        response.setName(profile.getName());
        response.setPhone(profile.getPhone());
        response.setAvatarUrl(profile.getAvatarUrl());
        response.setSkills(fromJson(profile.getSkills()));
        response.setAvailableDays(fromJson(profile.getAvailableDays()));
        response.setGender(profile.getGender());
        response.setBirthday(profile.getBirthday());
        response.setCreatedAt(profile.getCreatedAt());
        response.setUpdatedAt(profile.getUpdatedAt());
        return response;
    }

    private String toJson(List<String> list) {
        try {
            return objectMapper.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize list to JSON", e);
        }
    }

    private List<String> fromJson(String json) {
        if (json == null || json.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse JSON list: " + json, e);
        }
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
