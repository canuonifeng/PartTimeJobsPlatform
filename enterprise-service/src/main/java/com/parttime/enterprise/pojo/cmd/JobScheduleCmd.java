package com.parttime.enterprise.pojo.cmd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class JobScheduleCmd {

    @Schema(description = "排班ID（编辑时传入）")
    private Long id;
    @Schema(description = "排班日期")
    private LocalDate scheduleDate;
    @Schema(description = "开始时间")
    private LocalTime startTime;
    @Schema(description = "结束时间")
    private LocalTime endTime;
    @Schema(description = "可报名人数")
    private Integer slotsAvailable;
}
