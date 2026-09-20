package com.parttime.platform.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "交易概览")
public class TransactionOverviewVO {
    @Schema(description = "今日充值总额")
    private BigDecimal todayTopUpAmount;
    @Schema(description = "今日提现总额")
    private BigDecimal todayWithdrawalAmount;
    @Schema(description = "今日结算总额")
    private BigDecimal todaySettlementAmount;
    @Schema(description = "今日服务费收入")
    private BigDecimal todayServiceFeeAmount;
}
