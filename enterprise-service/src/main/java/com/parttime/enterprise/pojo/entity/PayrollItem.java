package com.parttime.enterprise.pojo.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PayrollItem {

    private Long id;
    private Long batchId;
    private Long workerId;
    private Long jobId;
    private BigDecimal totalHours;
    private String rateType;
    private BigDecimal rateAmount;
    private BigDecimal totalPay;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
