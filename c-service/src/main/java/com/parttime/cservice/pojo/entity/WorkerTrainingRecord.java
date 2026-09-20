package com.parttime.cservice.pojo.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class WorkerTrainingRecord {

    @Schema(description = "记录ID")
    private Long id;
    @Schema(description = "工人ID")
    private Long workerId;
    @Schema(description = "课程ID")
    private Long courseId;
    @Schema(description = "状态 IN_PROGRESS/COMPLETED/FAILED")
    private String status;
    @Schema(description = "考试得分（百分制）")
    private Integer score;
    @Schema(description = "开始学习时间")
    private LocalDateTime startedAt;
    @Schema(description = "完成时间")
    private LocalDateTime completedAt;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
