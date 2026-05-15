package com.parttime.enterprise.pojo.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NotificationLog {

    @Schema(description = "通知日志ID")
    private Long id;
    @Schema(description = "接收者ID")
    private Long recipientId;
    @Schema(description = "接收者类型: ENTERPRISE-企业, WORKER-工人")
    private String recipientType;
    @Schema(description = "通知类型")
    private String type;
    @Schema(description = "发送渠道: SMS-短信, EMAIL-邮件, APP_PUSH-应用推送")
    private String channel;
    @Schema(description = "通知标题")
    private String title;
    @Schema(description = "通知内容")
    private String content;
    @Schema(description = "发送状态: PENDING-待发送, SENT-已发送, FAILED-发送失败")
    private String status;
    @Schema(description = "错误信息")
    private String errorMessage;
    @Schema(description = "发送时间")
    private LocalDateTime sentAt;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
