package com.parttime.platform.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CsMessageVO {
    @Schema(description = "消息ID")
    private Long id;
    @Schema(description = "会话ID")
    private Long sessionId;
    @Schema(description = "发送方类型")
    private String senderType;
    @Schema(description = "发送方名称")
    private String senderName;
    @Schema(description = "消息内容")
    private String content;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
