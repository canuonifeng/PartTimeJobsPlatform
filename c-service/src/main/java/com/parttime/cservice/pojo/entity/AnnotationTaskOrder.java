package com.parttime.cservice.pojo.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AnnotationTaskOrder {
    @Schema(description = "任务单ID")
    private Long id;
    @Schema(description = "报名ID")
    private Long applicationId;
    @Schema(description = "排班ID")
    private Long scheduleId;
    @Schema(description = "工人ID")
    private Long workerId;
    @Schema(description = "岗位ID")
    private Long jobId;
    @Schema(description = "已完成数量")
    private Integer itemsCompleted;
    @Schema(description = "状态: PENDING-待提交, SUBMITTED-已提交, REVIEWED-已审核, SETTLED-已结算")
    private String status;
    @Schema(description = "外部提交ID")
    private String externalSubmissionId;
    @Schema(description = "提交时间")
    private LocalDateTime submittedAt;
    @Schema(description = "完成时间")
    private LocalDateTime completedAt;
    @Schema(description = "结算时间")
    private LocalDateTime settledAt;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
