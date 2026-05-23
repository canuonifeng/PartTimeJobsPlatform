package com.parttime.cservice.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class EarningsSummaryVO {

    @Schema(description = "总收入")
    private BigDecimal totalEarned;
    @Schema(description = "已提现")
    private BigDecimal totalWithdrawn;
    @Schema(description = "待提现")
    private BigDecimal pendingWithdrawal;
}
