package com.parttime.platform.pojo.cmd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class CsSessionQueryCmd {
    @Schema(description = "状态: WAITING, PROCESSING, CLOSED")
    private String status;
    @Schema(description = "用户类型: WORKER, ENTERPRISE")
    private String userType;
    @Schema(description = "关键词")
    private String keyword;
}
