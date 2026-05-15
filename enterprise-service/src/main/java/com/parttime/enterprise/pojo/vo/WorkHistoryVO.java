package com.parttime.enterprise.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class WorkHistoryVO {

    @Schema(description = "班次ID")
    private Long shiftId;
    @Schema(description = "岗位ID")
    private Long jobId;
    @Schema(description = "岗位标题")
    private String jobTitle;
    @Schema(description = "班次日期")
    private LocalDate shiftDate;
    @Schema(description = "开始时间")
    private LocalTime startTime;
    @Schema(description = "结束时间")
    private LocalTime endTime;
}
