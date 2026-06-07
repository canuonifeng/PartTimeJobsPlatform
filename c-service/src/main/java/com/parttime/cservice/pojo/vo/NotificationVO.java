package com.parttime.cservice.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NotificationVO {

    @Schema(description = "通知ID")
    private Long id;
    @Schema(description = "接收者ID")
    private Long recipientId;
    @Schema(description = "接收者类型")
    private String recipientType;
    @Schema(description = "通知类型")
    private String type;
    @Schema(description = "消息分类")
    private String category;
    @Schema(description = "通知标题")
    private String title;
    @Schema(description = "通知内容")
    private String content;
    @Schema(description = "发送状态")
    private String status;
    @Schema(description = "是否已读")
    private Boolean read;
    @Schema(description = "关联业务类型")
    private String relatedType;
    @Schema(description = "关联业务ID")
    private Long relatedId;
    @Schema(description = "发送时间")
    private LocalDateTime sentAt;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
