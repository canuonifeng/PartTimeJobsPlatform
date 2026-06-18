package com.parttime.enterprise.pojo.cmd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ScheduleExportCmd {
    @Schema(description = "班次ID")
    private Long scheduleId;
    @Schema(description = "报名状态")
    private String status;
}
