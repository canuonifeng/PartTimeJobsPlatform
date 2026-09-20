package com.parttime.platform.pojo.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class RiskBlacklist {
    private Long id;
    private String targetType;
    private Long targetId;
    private String targetName;
    private String targetValue;
    private String reason;
    private String riskLevel;
    private String banType;
    private LocalDateTime banStartTime;
    private LocalDateTime banEndTime;
    private Long operatorId;
    private String operatorName;
    private String status;
    private String removedReason;
    private LocalDateTime removedAt;
    private Long removedById;
    private String removedByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
