package com.parttime.platform.pojo.cmd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class QuestionUpdateCmd {

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
    @Schema(description = "正确答案；单选/判断传单个key，多选传逗号分隔或JSON数组")
    private String answer;
    @Schema(description = "解析")
    private String analysis;
    @Schema(description = "排序号")
    private Integer sortOrder;

    @Data
    public static class OptionDTO {
        @Schema(description = "选项key")
        private String key;
        @Schema(description = "选项文案")
        private String label;
    }
}
