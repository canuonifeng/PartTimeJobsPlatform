package com.parttime.enterprise.pojo.cmd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class ScheduleManageUpdateCmd {
    @Schema(description = "班次ID")
    private Long id;
    @Schema(description = "班次名称")
    private String scheduleName;
    @Schema(description = "班次日期")
    private LocalDate scheduleDate;
    @Schema(description = "开始时间")
    private LocalTime startTime;
    @Schema(description = "结束时间")
    private LocalTime endTime;
    @Schema(description = "招聘人数")
    private Integer slotsAvailable;
    @Schema(description = "联系人姓名")
    private String contactName;
    @Schema(description = "联系人电话")
    private String contactPhone;
    @Schema(description = "状态")
    private String status;
}
