package com.parttime.cservice.pojo.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ProfileVO {

    private Long workerId;
    private String name;
    private String phone;
    private String avatarUrl;
    private String skills;
    private String availableDays;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
