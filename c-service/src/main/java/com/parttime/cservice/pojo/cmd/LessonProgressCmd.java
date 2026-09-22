package com.parttime.cservice.pojo.cmd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class LessonProgressCmd {

    @Schema(description = "课时ID")
    private Long lessonId;
    @Schema(description = "学习进度（百分比）")
    private Integer progress;
}
