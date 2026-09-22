package com.parttime.cservice.pojo.cmd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class LessonCompleteCmd {

    @Schema(description = "课时ID")
    private Long lessonId;
}
