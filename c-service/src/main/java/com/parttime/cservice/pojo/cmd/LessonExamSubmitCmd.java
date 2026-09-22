package com.parttime.cservice.pojo.cmd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class LessonExamSubmitCmd {

    @Schema(description = "课时ID")
    private Long lessonId;
    @Schema(description = "答案列表")
    private List<LessonExamAnswerItem> answers;
}
