package com.parttime.cservice.pojo.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class WorkerShiftVO {

    private Long shiftId;
    private Long jobId;
    private String jobTitle;
    private String jobLocation;
    private LocalDate shiftDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String status;
    private BigDecimal locationLat;
    private BigDecimal locationLng;
    private Integer locationRadius;
    private String locationName;
}
