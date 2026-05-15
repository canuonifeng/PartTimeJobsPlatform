package com.parttime.enterprise.pojo.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AttendanceRecord {

    private Long id;
    private Long shiftId;
    private LocalDateTime checkInTime;
    private BigDecimal checkInLat;
    private BigDecimal checkInLng;
    private LocalDateTime checkOutTime;
    private BigDecimal checkOutLat;
    private BigDecimal checkOutLng;
    private BigDecimal totalHours;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
