package com.parttime.enterprise.pojo.cmd;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class ScheduleShiftCmd {

    private Long jobId;
    private Long templateSlotId;
    private Long workerId;
    private LocalDate shiftDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private BigDecimal locationLat;
    private BigDecimal locationLng;
    private Integer locationRadius;
    private String locationName;
}
