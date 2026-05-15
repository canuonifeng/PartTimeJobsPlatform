package com.parttime.enterprise.pojo.vo;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class WorkHistoryVO {

    private Long shiftId;
    private Long jobId;
    private String jobTitle;
    private LocalDate shiftDate;
    private LocalTime startTime;
    private LocalTime endTime;
}
