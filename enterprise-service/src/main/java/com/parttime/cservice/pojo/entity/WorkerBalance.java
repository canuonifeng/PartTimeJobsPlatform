package com.parttime.cservice.pojo.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class WorkerBalance {
    @Schema(description = "工人ID")
    private Long workerId;
    @Schema(description = "可用余额")
    private BigDecimal balance;
    @Schema(description = "累计收入")
    private BigDecimal totalEarned;
    @Schema(description = "累计提现")
    private BigDecimal totalWithdrawn;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
