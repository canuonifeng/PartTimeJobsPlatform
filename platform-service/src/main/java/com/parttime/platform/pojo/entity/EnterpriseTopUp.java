package com.parttime.platform.pojo.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class EnterpriseTopUp {
    @Schema(description = "ID")
    private Long id;
    @Schema(description = "企业ID")
    private Long enterpriseId;
    @Schema(description = "企业名称")
    private String enterpriseName;
    @Schema(description = "充值金额")
    private BigDecimal amount;
    @Schema(description = "支付方式")
    private String paymentMethod;
    @Schema(description = "凭证图片URL")
    private String receiptUrl;
    @Schema(description = "状态")
    private String status;
    @Schema(description = "审核人")
    private String auditor;
    @Schema(description = "审核时间")
    private LocalDateTime auditedAt;
    @Schema(description = "审核备注")
    private String auditRemark;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
