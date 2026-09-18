package com.parttime.enterprise.pojo.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AnnotationTaskOrder {

    @Schema(description = "标注任务订单ID")
    private Long id;
    @Schema(description = "排班ID")
    private Long jobScheduleId;
    @Schema(description = "工人ID")
    private Long workerId;
    @Schema(description = "企业ID")
    private Long companyId;
    @Schema(description = "订单状态: PENDING/IN_PROGRESS/SUBMITTED/COMPLETED/REJECTED")
    private String status;
    @Schema(description = "完成数量")
    private Integer completedItems;
    @Schema(description = "总数量")
    private Integer totalItems;
    @Schema(description = "计价模式: PER_ITEM/PER_PACKAGE")
    private String pricingMode;
    @Schema(description = "单价")
    private BigDecimal unitPrice;
    @Schema(description = "总金额")
    private BigDecimal totalAmount;
    @Schema(description = "外部任务ID")
    private String externalTaskId;
    @Schema(description = "外部系统类型")
    private String externalSystemType;
    @Schema(description = "备注")
    private String remark;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
