package com.parttime.enterprise.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class AnnotationBatchVO {

    @Schema(description = "批次ID")
    private Long id;
    @Schema(description = "岗位ID")
    private Long jobId;
    @Schema(description = "批次编码")
    private String batchCode;
    @Schema(description = "总数量")
    private Integer totalItems;
    @Schema(description = "外部批次ID")
    private String externalBatchId;
    @Schema(description = "状态 ACTIVE/CANCELLED")
    private String status;
    @Schema(description = "已抢任务单数")
    private Integer grabbedCount;
    @Schema(description = "已完成任务单数")
    private Integer completedCount;
    @Schema(description = "完成进度百分比 0-100")
    private Integer progress;
}
