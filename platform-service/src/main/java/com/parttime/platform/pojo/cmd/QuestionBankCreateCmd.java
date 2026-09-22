package com.parttime.platform.pojo.cmd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class QuestionBankCreateCmd {

    @Schema(description = "题库名称")
    private String name;
    @Schema(description = "题库描述")
    private String description;
}
