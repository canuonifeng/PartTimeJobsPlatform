package com.parttime.platform.pojo.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class RiskRuleVO {
    private Long id;
    private String ruleName;
    private String ruleCode;
    private String ruleType;
    private String description;
    private String triggerCondition;
    private String actionType;
    private String actionConfig;
    private String riskLevel;
    private String status;
    private Integer triggerCount;
    private LocalDateTime lastTriggerAt;
    private String operatorName;
    private LocalDateTime createdAt;
}
