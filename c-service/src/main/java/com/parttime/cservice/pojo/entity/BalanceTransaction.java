package com.parttime.cservice.pojo.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class BalanceTransaction {
    @Schema(description = "交易ID")
    private Long id;
    @Schema(description = "工人ID")
    private Long workerId;
    @Schema(description = "变动金额")
    private BigDecimal amount;
    @Schema(description = "类型: EARNINGS/WITHDRAWAL")
    private String type;
    @Schema(description = "历史结算账单ID")
    private Long relatedBillId;
    @Schema(description = "关联考勤记录ID")
    private Long relatedAttendanceRecordId;
    @Schema(description = "关联提现记录ID")
    private Long relatedWithdrawalId;
    @Schema(description = "描述")
    private String description;
    private LocalDateTime createdAt;
}
