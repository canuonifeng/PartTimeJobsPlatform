package com.parttime.platform.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ReferralAuditVO {

    @Schema(description = "奖励ID")
    private Long id;
    @Schema(description = "奖励金额")
    private BigDecimal amount;
    @Schema(description = "状态")
    private String status;
    @Schema(description = "创建时间")
    private String createdAt;
}