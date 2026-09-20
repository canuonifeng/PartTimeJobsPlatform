package com.parttime.platform.pojo.cmd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class WhitelistSaveCmd {
    @Schema(description = "目标类型: WORKER/ENTERPRISE/IP/PHONE")
    private String targetType;
    @Schema(description = "目标ID")
    private Long targetId;
    @Schema(description = "目标名称")
    private String targetName;
    @Schema(description = "目标值（手机号/IP等）")
    private String targetValue;
    @Schema(description = "白名单原因")
    private String reason;
    @Schema(description = "生效结束时间")
    private LocalDateTime effectiveEndTime;
    @Schema(description = "操作人姓名")
    private String operatorName;
}
