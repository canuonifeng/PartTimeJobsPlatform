package com.parttime.enterprise.pojo.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class EnterpriseTransactionVO {
    private Long id;
    private BigDecimal amount;
    private String type;           // TOP_UP / SETTLEMENT / SETTLEMENT_REFUND
    private Long relatedBillId;
    private String description;
    private String createdAt;      // Beijing time string
}
