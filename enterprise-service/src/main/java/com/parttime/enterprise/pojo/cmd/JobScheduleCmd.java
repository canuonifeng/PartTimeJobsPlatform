package com.parttime.enterprise.pojo.cmd;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class JobScheduleCmd {

    private LocalDate scheduleDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer slotsAvailable;
}
