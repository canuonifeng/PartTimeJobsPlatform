package com.parttime.platform.pojo.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class WithdrawalRecordVO {
    private Long id;
    private Long workerId;
    private String workerName;
    private String workerPhone;
    private BigDecimal amount;
    private String status;
    private String bankInfo;
    private String remark;
    private LocalDateTime requestedAt;
    private LocalDateTime processedAt;
    private LocalDateTime completedAt;
    private String thirdPartySerialNo;
    private String thirdPartyPlatform;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
