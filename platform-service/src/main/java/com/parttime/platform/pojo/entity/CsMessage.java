package com.parttime.platform.pojo.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CsMessage {

    @Schema(description = "消息ID")
    private Long id;
    @Schema(description = "会话ID")
    private Long sessionId;
    @Schema(description = "发送方: USER, AGENT, SYSTEM")
    private String senderType;
    @Schema(description = "发送方名称")
    private String senderName;
    @Schema(description = "消息内容")
    private String content;
    @Schema(description = "是否已读")
    private Integer isRead;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
