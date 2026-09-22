package com.parttime.platform.pojo.cmd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class TrainingLessonQueryCmd {

    @Schema(description = "所属课程ID")
    private Long courseId;
}
