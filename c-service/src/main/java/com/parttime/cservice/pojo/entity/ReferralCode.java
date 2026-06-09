package com.parttime.cservice.pojo.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReferralCode {
    private Long id;
    private Long workerId;
    private String code;
    private LocalDateTime createdAt;
}
