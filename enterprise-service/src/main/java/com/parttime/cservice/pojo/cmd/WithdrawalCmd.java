package com.parttime.cservice.pojo.cmd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class WithdrawalCmd {

    @Schema(description = "提现金额")
    private BigDecimal amount;
}
