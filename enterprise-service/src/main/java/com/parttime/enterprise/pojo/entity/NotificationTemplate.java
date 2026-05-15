package com.parttime.enterprise.pojo.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NotificationTemplate {

    @Schema(description = "模板ID")
    private Long id;
    @Schema(description = "通知类型")
    private String type;
    @Schema(description = "发送渠道: SMS-短信, EMAIL-邮件, APP_PUSH-应用推送")
    private String channel;
    @Schema(description = "标题模板")
    private String titleTemplate;
    @Schema(description = "内容模板")
    private String contentTemplate;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
