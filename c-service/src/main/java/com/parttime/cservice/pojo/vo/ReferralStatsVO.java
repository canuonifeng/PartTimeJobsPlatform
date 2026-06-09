package com.parttime.cservice.pojo.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ReferralStatsVO {
    private int totalReferees;
    private BigDecimal totalRewardAmount;
    private BigDecimal pendingRewardAmount;
}
