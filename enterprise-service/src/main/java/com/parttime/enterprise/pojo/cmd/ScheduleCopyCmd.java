package com.parttime.enterprise.pojo.cmd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class ScheduleCopyCmd {
    @Schema(description = "源班次ID")
    private Long sourceScheduleId;
    @Schema(description = "新日期")
    private LocalDate scheduleDate;
    @Schema(description = "新开始时间")
    private LocalTime startTime;
    @Schema(description = "新结束时间")
    private LocalTime endTime;
}
