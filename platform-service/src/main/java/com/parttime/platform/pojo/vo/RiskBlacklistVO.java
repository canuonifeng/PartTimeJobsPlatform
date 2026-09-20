package com.parttime.platform.pojo.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class RiskBlacklistVO {
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
    private String operatorName;
    private String status;
    private LocalDateTime createdAt;
}
