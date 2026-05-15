package com.parttime.enterprise.pojo.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class WithdrawalVO {

    private Long id;
    private Long workerId;
    private BigDecimal amount;
    private String status;
    private LocalDateTime requestedAt;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
