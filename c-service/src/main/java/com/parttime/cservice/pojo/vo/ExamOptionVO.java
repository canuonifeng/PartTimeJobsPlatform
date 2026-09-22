package com.parttime.cservice.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ExamOptionVO {

    @Schema(description = "选项 key")
    private String key;
    @Schema(description = "选项文案")
    private String label;
}
