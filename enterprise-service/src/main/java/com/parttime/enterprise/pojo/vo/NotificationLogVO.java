package com.parttime.enterprise.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NotificationLogVO {

    @Schema(description = "通知日志ID")
    private Long id;
    @Schema(description = "接收者ID")
    private Long recipientId;
    @Schema(description = "接收者类型")
    private String recipientType;
    @Schema(description = "通知类型")
    private String type;
    @Schema(description = "发送渠道")
    private String channel;
    @Schema(description = "通知标题")
    private String title;
    @Schema(description = "通知内容")
    private String content;
    @Schema(description = "发送状态")
    private String status;
    @Schema(description = "错误信息")
    private String errorMessage;
    @Schema(description = "发送时间")
    private LocalDateTime sentAt;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
