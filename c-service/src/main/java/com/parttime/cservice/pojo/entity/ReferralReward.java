package com.parttime.cservice.pojo.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ReferralReward {
    private Long id;
    private Long referralRecordId;
    private BigDecimal amount;
    private String status;       // PENDING, AUDITING, GRANTED, REJECTED, FAILED
    private String auditRemark;
    private LocalDateTime grantedAt;
    private LocalDateTime createdAt;
}
