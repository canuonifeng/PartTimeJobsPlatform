package com.parttime.platform.pojo.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Worker {
    private Long id;
    private String name;
    private String phone;
    private String wechatCode;
    private String openId;
    private String avatarUrl;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer creditScore;
}
