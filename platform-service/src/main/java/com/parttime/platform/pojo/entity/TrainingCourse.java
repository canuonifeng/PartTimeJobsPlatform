package com.parttime.platform.pojo.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TrainingCourse {

    @Schema(description = "课程ID")
    private Long id;
    @Schema(description = "关联技能认证ID")
    private Long certificationId;
    @Schema(description = "课程标题")
    private String title;
    @Schema(description = "课程摘要")
    private String summary;
    @Schema(description = "课程内容（Markdown/HTML）")
    private String content;
    @Schema(description = "考试题目 JSON")
    private String examJson;
    @Schema(description = "及格分（百分制）")
    private Integer passScore;
    @Schema(description = "状态 DRAFT/PUBLISHED/OFFLINE")
    private String status;
    @Schema(description = "排序号")
    private Integer sortOrder;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
