package com.parttime.platform.pojo.cmd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ReviewViolationCmd {
    @Schema(description = "评价ID")
    private Long id;
    @Schema(description = "违规原因")
    private String violationReason;
    @Schema(description = "处理人姓名")
    private String operatorName;
}
