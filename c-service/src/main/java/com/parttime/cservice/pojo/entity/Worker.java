package com.parttime.cservice.pojo.entity;

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
    private String nickname;
    private String avatar;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Worker() {}
}
