package com.parttime.cservice.pojo.entity;

import java.time.LocalDateTime;

public class WorkerProfile {

    private Long id;
    private Long workerId;
    private String name;
    private String phone;
    private String avatarUrl;
    private String skills;
    private String availableDays;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public WorkerProfile() {}

    public WorkerProfile(Long workerId, String name, String phone, String avatarUrl) {
        this.workerId = workerId;
        this.name = name;
        this.phone = phone;
        this.avatarUrl = avatarUrl;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getWorkerId() { return workerId; }
    public void setWorkerId(Long workerId) { this.workerId = workerId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public String getSkills() { return skills; }
    public void setSkills(String skills) { this.skills = skills; }

    public String getAvailableDays() { return availableDays; }
    public void setAvailableDays(String availableDays) { this.availableDays = availableDays; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
