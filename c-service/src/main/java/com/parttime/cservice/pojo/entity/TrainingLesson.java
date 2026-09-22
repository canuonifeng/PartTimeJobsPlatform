package com.parttime.cservice.pojo.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TrainingLesson {

    @Schema(description = "课时ID")
    private Long id;
    @Schema(description = "所属课程ID")
    private Long courseId;
    @Schema(description = "课时类型 VIDEO/AUDIO/DOCUMENT/IMAGE_TEXT/EXAM")
    private String lessonType;
    @Schema(description = "课时标题")
    private String title;
    @Schema(description = "文档/图文正文")
    private String content;
    @Schema(description = "音视频/图片地址")
    private String mediaUrl;
    @Schema(description = "预计时长（分钟）")
    private Integer durationMinutes;
    @Schema(description = "考试配置 JSON")
    private String examConfigJson;
    @Schema(description = "排序号")
    private Integer sortOrder;
    @Schema(description = "状态 DRAFT/PUBLISHED/OFFLINE")
    private String status;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
