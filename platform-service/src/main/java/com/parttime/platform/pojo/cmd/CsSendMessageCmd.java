package com.parttime.platform.pojo.cmd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class CsSendMessageCmd {
    @Schema(description = "会话ID")
    private Long sessionId;
    @Schema(description = "消息内容")
    private String content;
}
