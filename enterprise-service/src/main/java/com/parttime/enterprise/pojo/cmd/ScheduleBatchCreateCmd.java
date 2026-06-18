package com.parttime.enterprise.pojo.cmd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
public class ScheduleBatchCreateCmd {
    @Schema(description = "岗位ID")
    private Long jobId;
    @Schema(description = "开始日期")
    private LocalDate startDate;
    @Schema(description = "结束日期")
    private LocalDate endDate;
    @Schema(description = "周几，1-7")
    private List<Integer> weekdays;
    @Schema(description = "开始时间")
    private LocalTime startTime;
    @Schema(description = "结束时间")
    private LocalTime endTime;
}
