package com.parttime.platform.pojo.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class SettlementBill {
    private Long id;
    private Long companyId;
    private String companyName;
    private Long jobId;
    private Long shiftId;
    private Long workerId;
    private String workerName;
    private LocalDate shiftDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private BigDecimal totalHours;
    private String rateType;
    private BigDecimal rateAmount;
    private BigDecimal scheduledPay;
    private BigDecimal actualPay;
    private String serialNumber;
    private String thirdPartySerialNo;
    private String thirdPartyPlatform;
    private String status;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
