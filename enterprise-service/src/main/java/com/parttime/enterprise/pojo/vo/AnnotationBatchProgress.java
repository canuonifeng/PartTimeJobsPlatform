package com.parttime.enterprise.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class AnnotationBatchProgress {

    @Schema(description = "排班/批次ID")
    private Long scheduleId;
    @Schema(description = "已抢任务单数")
    private Integer grabbedCount;
    @Schema(description = "已完成任务单数")
    private Integer completedCount;
}
