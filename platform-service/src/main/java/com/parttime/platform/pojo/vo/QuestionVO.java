package com.parttime.platform.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class QuestionVO {

    @Schema(description = "题目ID")
    private Long id;
    @Schema(description = "所属题库ID")
    private Long bankId;
    @Schema(description = "题型 SINGLE_CHOICE/MULTIPLE_CHOICE/JUDGE")
    private String questionType;
    @Schema(description = "题干")
    private String stem;
    @Schema(description = "选项列表")
    private List<OptionDTO> options;
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

    @Data
    public static class OptionDTO {
        @Schema(description = "选项key")
        private String key;
        @Schema(description = "选项文案")
        private String label;
    }
}
