package com.parttime.cservice.pojo.entity;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class JobSchedule {
    private Long id;
    private Long jobId;
    private LocalDate scheduleDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer slotsAvailable;
    private String status;
}
