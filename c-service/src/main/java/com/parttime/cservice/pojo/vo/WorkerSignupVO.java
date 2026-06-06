package com.parttime.cservice.pojo.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class WorkerSignupVO {
    private Long applicationId;
    private Long jobId;
    private Long scheduleId;
    private String jobTitle;
    private String companyName;
    private String status;
    private LocalDate workDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String location;
    private BigDecimal payAmount;
    private String payType;
    private LocalDateTime appliedAt;
}
