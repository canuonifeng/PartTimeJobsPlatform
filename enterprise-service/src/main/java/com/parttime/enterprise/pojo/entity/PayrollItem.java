package com.parttime.enterprise.pojo.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PayrollItem {

    @Schema(description = "薪资项ID")
    private Long id;
    @Schema(description = "批次ID")
    private Long batchId;
    @Schema(description = "工人ID")
    private Long workerId;
    @Schema(description = "岗位ID")
    private Long jobId;
    @Schema(description = "总工时")
    private BigDecimal totalHours;
    @Schema(description = "薪资类型")
    private String rateType;
    @Schema(description = "薪资单价")
    private BigDecimal rateAmount;
    @Schema(description = "总薪资")
    private BigDecimal totalPay;
    @Schema(description = "薪资项状态: PENDING-待处理, CALCULATED-已计算, PAID-已支付")
    private String status;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
