package com.parttime.platform.pojo.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class EnterpriseBalanceTransaction {
    private Long id;
    private Long companyId;
    private BigDecimal amount;
    private String type;
    private Long relatedBillId;
    private Long relatedTopUpId;
    private String description;
    private String companyName;
    private LocalDateTime createdAt;
}
