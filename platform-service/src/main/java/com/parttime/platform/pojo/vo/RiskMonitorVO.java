package com.parttime.platform.pojo.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class RiskMonitorVO {
    private Long id;
    private String sourceType;
    private String title;
    private String level;
    private String targetName;
    private String status;
    private LocalDateTime createdAt;
}
