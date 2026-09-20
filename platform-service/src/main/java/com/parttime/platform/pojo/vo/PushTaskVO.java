package com.parttime.platform.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "推送任务VO")
public class PushTaskVO {

    @Schema(description = "推送任务ID")
    private Long id;
    @Schema(description = "推送标题")
    private String title;
    @Schema(description = "推送内容")
    private String content;
    @Schema(description = "目标类型")
    private String targetType;
    @Schema(description = "推送类型")
    private String pushType;
    @Schema(description = "定时推送时间")
    private LocalDateTime scheduledTime;
    @Schema(description = "状态")
    private String status;
    @Schema(description = "目标总数")
    private Integer totalCount;
    @Schema(description = "成功数量")
    private Integer successCount;
    @Schema(description = "失败数量")
    private Integer failCount;
    @Schema(description = "操作人姓名")
    private String operatorName;
    @Schema(description = "完成时间")
    private LocalDateTime completedAt;
    @Schema(description = "备注")
    private String remark;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
