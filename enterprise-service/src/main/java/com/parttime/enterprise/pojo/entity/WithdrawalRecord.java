package com.parttime.enterprise.pojo.entity;

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
    @Schema(description = "申请时间")
    private LocalDateTime requestedAt;
    @Schema(description = "完成时间")
    private LocalDateTime completedAt;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
