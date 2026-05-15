package com.parttime.enterprise.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class AttendanceReportVO {

    @Schema(description = "班次ID")
    private Long shiftId;
    @Schema(description = "岗位ID")
    private Long jobId;
    @Schema(description = "工人ID")
    private Long workerId;
    @Schema(description = "班次日期")
    private LocalDate shiftDate;
    @Schema(description = "开始时间")
    private LocalTime startTime;
    @Schema(description = "结束时间")
    private LocalTime endTime;
    @Schema(description = "班次状态")
    private String shiftStatus;
    @Schema(description = "工人姓名")
    private String workerName;
    @Schema(description = "签到时间")
    private LocalDateTime checkInTime;
    @Schema(description = "签退时间")
    private LocalDateTime checkOutTime;
    @Schema(description = "总工时")
    private BigDecimal totalHours;
    @Schema(description = "考勤状态")
    private String attendanceStatus;
}
