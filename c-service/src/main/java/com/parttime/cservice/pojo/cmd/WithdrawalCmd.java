package com.parttime.cservice.pojo.cmd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class WithdrawalCmd {

    @Schema(description = "提现金额")
    private BigDecimal amount;

    @Schema(description = "提现方式: WECHAT-微信零钱, BANK_CARD-银行卡")
    private String withdrawalMethod;

    @Schema(description = "银行卡ID（银行卡提现时需要）")
    private Long bankAccountId;
}
