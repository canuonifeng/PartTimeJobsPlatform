package com.parttime.enterprise.pojo.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class EnterpriseTopUpRecord {
    private Long id;
    private Long companyId;
    private BigDecimal amount;
    private String status;           // PROCESSING / COMPLETED / FAILED
    private String serialNumber;
    private String thirdPartySerialNo;
    private String thirdPartyPlatform;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
