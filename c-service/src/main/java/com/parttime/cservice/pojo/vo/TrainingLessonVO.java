package com.parttime.cservice.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class TrainingLessonVO {

    @Schema(description = "课时ID")
    private Long id;
    @Schema(description = "课时类型 VIDEO/AUDIO/DOCUMENT/IMAGE_TEXT/EXAM")
    private String lessonType;
    @Schema(description = "课时标题")
    private String title;
    @Schema(description = "排序号")
    private Integer sortOrder;
    @Schema(description = "状态 DRAFT/PUBLISHED/OFFLINE")
    private String status;
    @Schema(description = "当前工人是否已完成")
    private Boolean completed;
    @Schema(description = "当前课时是否锁定")
    private Boolean locked;
    @Schema(description = "学习进度（百分比）")
    private Integer progress;
    @Schema(description = "考试得分")
    private Integer score;
}
