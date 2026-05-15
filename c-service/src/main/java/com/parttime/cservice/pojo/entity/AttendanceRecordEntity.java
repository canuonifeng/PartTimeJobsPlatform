package com.parttime.cservice.pojo.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AttendanceRecordEntity {

    private Long id;
    private Long shiftId;
    private Long workerId;
    private LocalDateTime checkInTime;
    private BigDecimal checkInLat;
    private BigDecimal checkInLng;
    private LocalDateTime checkOutTime;
    private BigDecimal checkOutLat;
    private BigDecimal checkOutLng;
    private BigDecimal totalHours;
    private String status;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public AttendanceRecordEntity() {}
}
