package com.parttime.platform.pojo.cmd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class TopUpReviewCmd {
    @Schema(description = "充值记录ID")
    private Long id;
    @Schema(description = "审核备注")
    private String remark;
}
