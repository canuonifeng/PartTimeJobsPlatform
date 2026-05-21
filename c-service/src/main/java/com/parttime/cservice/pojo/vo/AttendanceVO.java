package com.parttime.cservice.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AttendanceVO {

    @Schema(description = "考勤记录ID")
    private Long attendanceId;
    @Schema(description = "班次ID")
    private Long shiftId;
    @Schema(description = "签到时间")
    private LocalDateTime checkInTime;
    @Schema(description = "签退时间")
    private LocalDateTime checkOutTime;
    @Schema(description = "总工时")
    private BigDecimal totalHours;
    @Schema(description = "工资")
    private BigDecimal payAmount;
    @Schema(description = "工资计算时间")
    private LocalDateTime calculatedAt;
    @Schema(description = "考勤状态")
    private String status;
}
