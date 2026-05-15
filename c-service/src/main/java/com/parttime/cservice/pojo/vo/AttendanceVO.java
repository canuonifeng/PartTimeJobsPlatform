package com.parttime.cservice.pojo.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AttendanceVO {

    private Long attendanceId;
    private Long shiftId;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    private BigDecimal totalHours;
    private String status;
}
