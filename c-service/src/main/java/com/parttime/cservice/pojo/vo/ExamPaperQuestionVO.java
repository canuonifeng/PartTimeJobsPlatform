package com.parttime.cservice.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class ExamPaperQuestionVO {

    @Schema(description = "题目ID")
    private Long questionId;
    @Schema(description = "题型 SINGLE_CHOICE/MULTIPLE_CHOICE/JUDGE")
    private String questionType;
    @Schema(description = "题干")
    private String stem;
    @Schema(description = "选项")
    private List<ExamOptionVO> options;
    @Schema(description = "该题分值")
    private Integer score;
}
