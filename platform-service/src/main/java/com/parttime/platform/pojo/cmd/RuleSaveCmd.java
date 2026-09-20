package com.parttime.platform.pojo.cmd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class RuleSaveCmd {
    @Schema(description = "规则ID（更新时传）")
    private Long id;
    @Schema(description = "规则名称")
    private String ruleName;
    @Schema(description = "规则代码")
    private String ruleCode;
    @Schema(description = "规则类型: REGISTRATION/LOGIN/APPLICATION/CLOCKIN/WITHDRAWAL/BEHAVIOR")
    private String ruleType;
    @Schema(description = "规则描述")
    private String description;
    @Schema(description = "触发条件（JSON）")
    private String triggerCondition;
    @Schema(description = "动作类型: ALERT/BLOCK/REVIEW/LIMIT")
    private String actionType;
    @Schema(description = "动作配置（JSON）")
    private String actionConfig;
    @Schema(description = "风险等级: LOW/MEDIUM/HIGH")
    private String riskLevel;
    @Schema(description = "状态: ACTIVE/INACTIVE")
    private String status;
    @Schema(description = "操作人姓名")
    private String operatorName;
}
