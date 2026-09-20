package com.parttime.platform.pojo.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class RiskWhitelist {
    private Long id;
    private String targetType;
    private Long targetId;
    private String targetName;
    private String targetValue;
    private String reason;
    private LocalDateTime effectiveStartTime;
    private LocalDateTime effectiveEndTime;
    private Long operatorId;
    private String operatorName;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
