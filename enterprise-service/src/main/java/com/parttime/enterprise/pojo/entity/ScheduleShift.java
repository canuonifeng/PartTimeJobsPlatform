package com.parttime.enterprise.pojo.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class ScheduleShift {

    private Long id;
    private Long jobId;
    private Long templateSlotId;
    private Long workerId;
    private LocalDate shiftDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private BigDecimal locationLat;
    private BigDecimal locationLng;
    private Integer locationRadius;
    private String locationName;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
