package com.parttime.cservice.pojo.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class EarningsSummaryVO {

    private BigDecimal totalEarned;
    private BigDecimal totalWithdrawn;
    private BigDecimal pendingWithdrawal;
}
