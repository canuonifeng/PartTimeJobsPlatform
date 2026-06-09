package com.parttime.cservice.pojo.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ReferralRewardVO {
    private Long id;
    private String refereeName;
    private BigDecimal amount;
    private String status;
    private String createdAt;
    private String grantedAt;
}
