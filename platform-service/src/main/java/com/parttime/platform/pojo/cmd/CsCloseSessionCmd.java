package com.parttime.platform.pojo.cmd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class CsCloseSessionCmd {
    @Schema(description = "会话ID")
    private Long sessionId;
    @Schema(description = "关闭原因")
    private String closeReason;
}
