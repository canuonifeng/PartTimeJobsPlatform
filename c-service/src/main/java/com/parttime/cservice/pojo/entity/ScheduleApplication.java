package com.parttime.cservice.pojo.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ScheduleApplication {
    private Long id;
    private Long scheduleId;
    private Long workerId;
    private String status;
    private LocalDateTime appliedAt;
    private LocalDateTime updatedAt;
}
