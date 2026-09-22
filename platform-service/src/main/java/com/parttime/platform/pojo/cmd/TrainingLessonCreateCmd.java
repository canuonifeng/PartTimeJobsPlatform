package com.parttime.platform.pojo.cmd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class TrainingLessonCreateCmd {

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
    private ExamConfigDTO examConfig;
    @Schema(description = "排序号")
    private Integer sortOrder;

    @Data
    public static class ExamConfigDTO {
        @Schema(description = "题库ID")
        private Long bankId;
        @Schema(description = "考试时长（分钟）")
        private Integer durationMinutes;
        @Schema(description = "及格分")
        private Integer passScore;
        @Schema(description = "出题规则")
        private List<ExamRuleDTO> rules;
    }

    @Data
    public static class ExamRuleDTO {
        @Schema(description = "题型")
        private String questionType;
        @Schema(description = "抽题数")
        private Integer count;
        @Schema(description = "每题分值")
        private Integer scorePer;
    }
}
