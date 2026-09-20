package com.parttime.platform.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CsSessionVO {
    @Schema(description = "会话ID")
    private Long id;
    @Schema(description = "会话编号")
    private String sessionNo;
    @Schema(description = "用户类型")
    private String userType;
    @Schema(description = "用户ID")
    private Long userId;
    @Schema(description = "用户名称")
    private String userName;
    @Schema(description = "用户电话")
    private String userPhone;
    @Schema(description = "接起客服名称")
    private String agentName;
    @Schema(description = "状态")
    private String status;
    @Schema(description = "最后一条消息")
    private String lastMessage;
    @Schema(description = "消息数")
    private Integer messageCount;
    @Schema(description = "未读消息数")
    private Integer unreadCount;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
    @Schema(description = "最后消息时间")
    private LocalDateTime lastMessageAt;
}
