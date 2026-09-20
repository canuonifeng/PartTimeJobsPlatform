package com.parttime.cservice.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class ExamQuestionVO {

    @Schema(description = "题目序号（从1开始）")
    private Integer index;
    @Schema(description = "题干")
    private String question;
    @Schema(description = "选项列表")
    private List<String> options;
}
