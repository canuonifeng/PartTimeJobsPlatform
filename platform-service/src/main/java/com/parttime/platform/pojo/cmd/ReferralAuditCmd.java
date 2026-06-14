package com.parttime.platform.pojo.cmd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ReferralAuditCmd {
    @Schema(description = "奖励记录ID")
    private Long id;
    @Schema(description = "审核备注")
    private String remark;
}
