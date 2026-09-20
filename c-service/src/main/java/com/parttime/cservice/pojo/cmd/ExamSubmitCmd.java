package com.parttime.cservice.pojo.cmd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class ExamSubmitCmd {

    @Schema(description = "课程ID")
    private Long courseId;

    @Schema(description = "答案列表，answers[i] 为第 i+1 题的答案索引")
    private List<Integer> answers;
}
