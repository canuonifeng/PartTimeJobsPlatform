package com.parttime.enterprise.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class PayrollBatchVO {

    @Schema(description = "批次ID")
    private Long id;
    @Schema(description = "企业ID")
    private Long companyId;
    @Schema(description = "批次名称")
    private String name;
    @Schema(description = "计薪开始日期")
    private LocalDate periodStart;
    @Schema(description = "计薪结束日期")
    private LocalDate periodEnd;
    @Schema(description = "批次状态")
    private String status;
    @Schema(description = "总金额")
    private BigDecimal totalAmount;
    @Schema(description = "工人数量")
    private Integer workerCount;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
