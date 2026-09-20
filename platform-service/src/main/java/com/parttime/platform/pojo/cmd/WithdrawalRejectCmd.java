package com.parttime.platform.pojo.cmd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class WithdrawalRejectCmd {
    @Schema(description = "提现记录ID")
    private Long id;
    @Schema(description = "拒绝原因")
    private String reason;
}
