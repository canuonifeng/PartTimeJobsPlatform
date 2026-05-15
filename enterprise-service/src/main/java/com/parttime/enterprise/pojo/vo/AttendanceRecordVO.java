package com.parttime.enterprise.pojo.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AttendanceRecordVO {

    private Long id;
    private Long shiftId;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    private BigDecimal totalHours;
    private String status;
    private LocalDateTime createdAt;
}
