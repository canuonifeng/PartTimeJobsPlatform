package com.parttime.platform.pojo.cmd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BlacklistSaveCmd {
    @Schema(description = "目标类型: WORKER/ENTERPRISE/IP/PHONE")
    private String targetType;
    @Schema(description = "目标ID")
    private Long targetId;
    @Schema(description = "目标名称")
    private String targetName;
    @Schema(description = "目标值（手机号/IP等）")
    private String targetValue;
    @Schema(description = "拉黑原因")
    private String reason;
    @Schema(description = "风险等级: LOW/MEDIUM/HIGH/SEVERE")
    private String riskLevel;
    @Schema(description = "封禁类型: TEMPORARY/PERMANENT")
    private String banType;
    @Schema(description = "封禁结束时间")
    private LocalDateTime banEndTime;
    @Schema(description = "操作人姓名")
    private String operatorName;
}
