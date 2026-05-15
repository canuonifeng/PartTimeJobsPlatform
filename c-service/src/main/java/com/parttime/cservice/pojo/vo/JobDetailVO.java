package com.parttime.cservice.pojo.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class JobDetailVO {
    private Long id;
    private String title;
    private String description;
    private String location;
    private String categoryName;
    private List<JobRateInfoVO> rates;
    private List<JobScheduleInfoVO> schedules;
    private Integer headcount;
    private Integer acceptedCount;
    private LocalDateTime deadline;
    private String status;
}
