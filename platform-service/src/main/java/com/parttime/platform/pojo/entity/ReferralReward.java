package com.parttime.platform.pojo.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ReferralReward {

    @Schema(description = "奖励ID")
    private Long id;
    @Schema(description = "推荐记录ID")
    private Long referralRecordId;
    @Schema(description = "奖励金额")
    private BigDecimal amount;
    @Schema(description = "状态")
    private String status;
    @Schema(description = "审核备注")
    private String auditRemark;
    @Schema(description = "发放时间")
    private LocalDateTime grantedAt;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}