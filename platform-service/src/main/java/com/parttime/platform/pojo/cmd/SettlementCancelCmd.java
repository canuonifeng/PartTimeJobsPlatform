package com.parttime.platform.pojo.cmd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class SettlementCancelCmd {
    @Schema(description = "结算流水ID")
    private Long id;
    @Schema(description = "撤销原因")
    private String reason;
}
