package com.parttime.cservice.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class AnnotationBatchProgress {

    @Schema(description = "排班/批次ID")
    private Long scheduleId;
    @Schema(description = "已抢任务单数")
    private Integer grabbedCount;
}
