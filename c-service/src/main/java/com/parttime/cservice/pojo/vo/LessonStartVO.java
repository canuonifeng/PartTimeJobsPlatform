package com.parttime.cservice.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class LessonStartVO {

    @Schema(description = "课时ID")
    private Long lessonId;
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
    @Schema(description = "当前学习进度（百分比）")
    private Integer currentProgress;
    @Schema(description = "考试试卷（仅 EXAM 课时返回）")
    private ExamPaperVO paper;
}
