package com.parttime.cservice.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class ExamPaperVO {

    @Schema(description = "课时ID")
    private Long lessonId;
    @Schema(description = "考试时长（分钟）")
    private Integer durationMinutes;
    @Schema(description = "试卷总分")
    private Integer totalScore;
    @Schema(description = "及格分")
    private Integer passScore;
    @Schema(description = "题目列表")
    private List<ExamPaperQuestionVO> questions;
}
