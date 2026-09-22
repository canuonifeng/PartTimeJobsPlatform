package com.parttime.platform.pojo.cmd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class QuestionListCmd {

    @Schema(description = "所属题库ID")
    private Long bankId;
}
