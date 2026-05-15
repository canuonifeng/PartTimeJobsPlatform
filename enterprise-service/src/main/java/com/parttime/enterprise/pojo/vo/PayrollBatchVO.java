package com.parttime.enterprise.pojo.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class PayrollBatchVO {

    private Long id;
    private Long companyId;
    private String name;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private String status;
    private BigDecimal totalAmount;
    private Integer workerCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
