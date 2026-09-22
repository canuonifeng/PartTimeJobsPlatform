package com.parttime.cservice.pojo.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class WorkerLessonRecord {

    @Schema(description = "记录ID")
    private Long id;
    @Schema(description = "工人ID")
    private Long workerId;
    @Schema(description = "课时ID")
    private Long lessonId;
    @Schema(description = "状态 IN_PROGRESS/COMPLETED/FAILED")
    private String status;
    @Schema(description = "学习进度（百分比）")
    private Integer progress;
    @Schema(description = "考试得分")
    private Integer score;
    @Schema(description = "考试快照 JSON")
    private String examSnapshotJson;
    @Schema(description = "考试尝试次数")
    private Integer examAttempts;
    @Schema(description = "开始学习时间")
    private LocalDateTime startedAt;
    @Schema(description = "完成时间")
    private LocalDateTime completedAt;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
