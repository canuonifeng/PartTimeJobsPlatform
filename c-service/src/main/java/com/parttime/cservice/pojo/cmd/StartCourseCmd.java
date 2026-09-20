package com.parttime.cservice.pojo.cmd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class StartCourseCmd {

    @Schema(description = "课程ID")
    private Long courseId;
}
