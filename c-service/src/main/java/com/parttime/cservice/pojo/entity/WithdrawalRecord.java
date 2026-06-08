package com.parttime.cservice.pojo.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class WithdrawalRecord {

    @Schema(description = "提现记录ID")
    private Long id;
    @Schema(description = "工人ID")
    private Long workerId;
    @Schema(description = "提现金额")
    private BigDecimal amount;
    @Schema(description = "提现状态: PENDING-待处理, PROCESSING-处理中, COMPLETED-已完成, FAILED-失败")
    private String status;
    @Schema(description = "银行信息")
    private String bankInfo;
    @Schema(description = "备注")
    private String remark;
    @Schema(description = "申请时间")
    private LocalDateTime requestedAt;
    @Schema(description = "处理时间")
    private LocalDateTime processedAt;
    @Schema(description = "完成时间")
    private LocalDateTime completedAt;
    @Schema(description = "第三方支付流水号")
    private String thirdPartySerialNo;
    @Schema(description = "第三方支付平台")
    private String thirdPartyPlatform;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
    @Schema(description = "提现方式: WECHAT-微信零钱, BANK_CARD-银行卡")
    private String withdrawalMethod;
    @Schema(description = "银行账户信息")
    private String bankAccount;
    @Schema(description = "微信OpenID")
    private String openId;

    public WithdrawalRecord() {}
}
