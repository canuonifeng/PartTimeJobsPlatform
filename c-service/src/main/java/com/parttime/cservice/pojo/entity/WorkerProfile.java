package com.parttime.cservice.pojo.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
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
}
