package com.parttime.cservice.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransactionVO {
    @Schema(description = "交易ID")
    private Long id;
    @Schema(description = "变动金额")
    private BigDecimal amount;
    @Schema(description = "类型: EARNINGS-收入, WITHDRAWAL-支出")
    private String type;
    @Schema(description = "描述")
    private String description;
    @Schema(description = "交易时间")
    private LocalDateTime createdAt;
}
