package com.parttime.platform.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class TrainingLessonVO {

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
    @Schema(description = "考试配置")
    private ExamConfigVO examConfig;
    @Schema(description = "考试总分")
    private Integer totalScore;
    @Schema(description = "排序号")
    private Integer sortOrder;
    @Schema(description = "状态 DRAFT/PUBLISHED/OFFLINE")
    private String status;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    @Data
    public static class ExamConfigVO {
        @Schema(description = "题库ID")
        private Long bankId;
        @Schema(description = "考试时长（分钟）")
        private Integer durationMinutes;
        @Schema(description = "及格分")
        private Integer passScore;
        @Schema(description = "出题规则")
        private List<ExamRuleVO> rules;
    }

    @Data
    public static class ExamRuleVO {
        @Schema(description = "题型")
        private String questionType;
        @Schema(description = "抽题数")
        private Integer count;
        @Schema(description = "每题分值")
        private Integer scorePer;
    }
}
