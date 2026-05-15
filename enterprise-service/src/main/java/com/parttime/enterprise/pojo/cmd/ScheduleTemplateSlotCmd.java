package com.parttime.enterprise.pojo.cmd;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalTime;

@Data
public class ScheduleTemplateSlotCmd {

    private Integer dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer maxWorkers;
    private BigDecimal locationLat;
    private BigDecimal locationLng;
    private Integer locationRadius;
    private String locationName;
}
