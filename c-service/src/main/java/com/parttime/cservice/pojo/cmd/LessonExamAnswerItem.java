package com.parttime.cservice.pojo.cmd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class LessonExamAnswerItem {

    @Schema(description = "题目ID")
    private Long questionId;
    @Schema(description = "答案：单选/判断传单 key；多选传 JSON 数组字符串")
    private String answer;
}
