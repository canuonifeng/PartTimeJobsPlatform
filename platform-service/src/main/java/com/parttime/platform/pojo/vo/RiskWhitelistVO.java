package com.parttime.platform.pojo.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class RiskWhitelistVO {
    private Long id;
    private String targetType;
    private Long targetId;
    private String targetName;
    private String targetValue;
    private String reason;
    private LocalDateTime effectiveStartTime;
    private LocalDateTime effectiveEndTime;
    private String operatorName;
    private String status;
    private LocalDateTime createdAt;
}
