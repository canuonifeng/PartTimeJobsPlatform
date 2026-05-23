package com.parttime.cservice.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class WithdrawalVO {

    @Schema(description = "提现记录ID")
    private Long id;
    @Schema(description = "工人ID")
    private Long workerId;
    @Schema(description = "提现金额")
    private BigDecimal amount;
    @Schema(description = "提现状态")
    private String status;
    @Schema(description = "申请时间")
    private LocalDateTime requestedAt;
    @Schema(description = "完成时间")
    private LocalDateTime completedAt;
    @Schema(description = "第三方支付流水号")
    private String thirdPartySerialNo;
    @Schema(description = "第三方支付平台")
    private String thirdPartyPlatform;
}
