package com.parttime.platform.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "每日对账")
public class DailyReconVO {
    @Schema(description = "日期")
    private String date;
    @Schema(description = "充值总额")
    private BigDecimal totalTopUp;
    @Schema(description = "提现总额")
    private BigDecimal totalWithdrawal;
    @Schema(description = "服务费收入")
    private BigDecimal totalServiceFee;
    @Schema(description = "结算笔数")
    private Integer settlementCount;
    @Schema(description = "对账状态")
    private String status;
}
