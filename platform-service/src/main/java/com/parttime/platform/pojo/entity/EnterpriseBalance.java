package com.parttime.platform.pojo.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class EnterpriseBalance {
    private Long companyId;
    private BigDecimal balance;
    private BigDecimal totalTopUp;
    private BigDecimal totalSpent;
    private BigDecimal creditLimit;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
