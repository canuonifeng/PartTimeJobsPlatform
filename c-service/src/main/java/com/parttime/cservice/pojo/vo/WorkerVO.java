package com.parttime.cservice.pojo.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class WorkerVO {
    private Long id;
    private String name;
    private String phone;
    private String avatar;
    private String openId;
    private String nickname;
    private String avatarUrl;
    private LocalDateTime createdAt;
}
