package com.parttime.enterprise.pojo.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class ScheduleTemplateSlot {

    private Long id;
    private Long templateId;
    private Integer dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer maxWorkers;
    private BigDecimal locationLat;
    private BigDecimal locationLng;
    private Integer locationRadius;
    private String locationName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
