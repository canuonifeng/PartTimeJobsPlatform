package com.parttime.enterprise.pojo.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class AttendanceReportVO {

    private Long shiftId;
    private Long jobId;
    private Long workerId;
    private LocalDate shiftDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String shiftStatus;
    private String workerName;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    private BigDecimal totalHours;
    private String attendanceStatus;
}
