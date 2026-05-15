package com.parttime.cservice.pojo.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class ShiftEntity {

    private Long id;
    private Long jobId;
    private String jobTitle;
    private String jobLocation;
    private Long workerId;
    private Long companyId;
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

    public ShiftEntity() {}
}
