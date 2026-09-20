package com.parttime.platform.pojo.cmd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "推送任务创建命令")
public class PushTaskCmd {

    @Schema(description = "推送标题")
    private String title;
    @Schema(description = "推送内容")
    private String content;
    @Schema(description = "目标类型: ALL, WORKER, ENTERPRISE, CUSTOM")
    private String targetType;
    @Schema(description = "目标ID列表，逗号分隔")
    private String targetIds;
    @Schema(description = "推送类型: IMMEDIATE, SCHEDULED")
    private String pushType;
    @Schema(description = "定时推送时间")
    private LocalDateTime scheduledTime;
    @Schema(description = "备注")
    private String remark;
}
