package com.parttime.platform.pojo.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class QuestionBankQuestion {

    @Schema(description = "题目ID")
    private Long id;
    @Schema(description = "所属题库ID")
    private Long bankId;
    @Schema(description = "题型 SINGLE_CHOICE/MULTIPLE_CHOICE/JUDGE")
    private String questionType;
    @Schema(description = "题干")
    private String stem;
    @Schema(description = "选项 JSON")
    private String optionsJson;
    @Schema(description = "正确答案")
    private String answer;
    @Schema(description = "解析")
    private String analysis;
    @Schema(description = "状态 DRAFT/PUBLISHED/OFFLINE")
    private String status;
    @Schema(description = "排序号")
    private Integer sortOrder;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
