package com.parttime.platform.pojo.cmd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class BlacklistQueryCmd {
    @Schema(description = "目标类型: WORKER/ENTERPRISE/IP/PHONE")
    private String targetType;
    @Schema(description = "状态: ACTIVE/EXPIRED/REMOVED")
    private String status;
    @Schema(description = "关键词")
    private String keyword;
}
