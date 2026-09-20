package com.parttime.cservice.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ExamResultVO {

    @Schema(description = "得分（百分制）")
    private Integer score;
    @Schema(description = "是否通过")
    private Boolean passed;
    @Schema(description = "及格分")
    private Integer passScore;
    @Schema(description = "提示信息")
    private String message;
}
