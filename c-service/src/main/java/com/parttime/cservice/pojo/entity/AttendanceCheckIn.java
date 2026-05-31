package com.parttime.cservice.pojo.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AttendanceCheckIn {
    private Long id;
    private Long shiftId;
    private Long workerId;
    private LocalDateTime checkInTime;
    private BigDecimal checkInLat;
    private BigDecimal checkInLng;
    private Integer lateSeconds;
    private Integer earlyLeaveSeconds;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
