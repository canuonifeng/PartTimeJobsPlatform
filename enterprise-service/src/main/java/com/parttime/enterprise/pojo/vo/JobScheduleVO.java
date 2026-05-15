package com.parttime.enterprise.pojo.vo;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class JobScheduleVO {

    private Long id;
    private Long jobId;
    private LocalDate scheduleDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer slotsAvailable;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
