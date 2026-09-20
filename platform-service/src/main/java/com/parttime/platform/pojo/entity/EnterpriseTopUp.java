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
    private Long companyId;
    @Schema(description = "企业名称")
    private String enterpriseName;
    @Schema(description = "充值金额")
    private BigDecimal amount;
    @Schema(description = "状态")
    private String status;
    @Schema(description = "流水号")
    private String serialNumber;
    @Schema(description = "第三方流水号")
    private String thirdPartySerialNo;
    @Schema(description = "第三方支付平台")
    private String thirdPartyPlatform;
    @Schema(description = "完成时间")
    private LocalDateTime completedAt;
    @Schema(description = "审核人")
    private String auditor;
    @Schema(description = "审核备注")
    private String auditRemark;
    @Schema(description = "审核时间")
    private LocalDateTime auditedAt;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
