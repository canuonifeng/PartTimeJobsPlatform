package com.parttime.platform.pojo.cmd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ReviewQueryCmd {
    @Schema(description = "评价类型: WORKER_TO_ENTERPRISE/ENTERPRISE_TO_WORKER")
    private String reviewType;
    @Schema(description = "是否违规")
    private Boolean isViolation;
    @Schema(description = "评分")
    private Integer rating;
    @Schema(description = "关键词")
    private String keyword;
}
