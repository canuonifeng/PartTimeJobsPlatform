package com.parttime.platform.pojo.cmd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class CreditScoreAdjustCmd {
    @Schema(description = "目标类型: WORKER/ENTERPRISE")
    private String targetType;
    @Schema(description = "目标ID")
    private Long targetId;
    @Schema(description = "调整分值（正负）")
    private Integer delta;
    @Schema(description = "调整原因")
    private String reason;
    @Schema(description = "操作人姓名")
    private String operatorName;
}
