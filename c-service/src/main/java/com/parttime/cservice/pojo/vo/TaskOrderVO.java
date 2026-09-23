package com.parttime.cservice.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TaskOrderVO {
    @Schema(description = "任务单ID")
    private Long id;
    @Schema(description = "岗位ID")
    private Long jobId;
    @Schema(description = "岗位标题")
    private String jobTitle;
    @Schema(description = "排班ID")
    private Long scheduleId;
    @Schema(description = "外部批次ID")
    private String externalBatchId;
    @Schema(description = "已完成数量")
    private Integer itemsCompleted;
    @Schema(description = "总数量")
    private Integer totalItems;
    @Schema(description = "状态")
    private String status;
    @Schema(description = "计价模式")
    private String pricingMode;
    @Schema(description = "单价")
    private BigDecimal unitPrice;
    @Schema(description = "总金额")
    private BigDecimal totalAmount;
    @Schema(description = "提交时间")
    private LocalDateTime submittedAt;
    @Schema(description = "完成时间")
    private LocalDateTime completedAt;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
