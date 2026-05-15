package com.parttime.enterprise.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AttendanceRecordVO {

    @Schema(description = "考勤记录ID")
    private Long id;
    @Schema(description = "班次ID")
    private Long shiftId;
    @Schema(description = "签到时间")
    private LocalDateTime checkInTime;
    @Schema(description = "签退时间")
    private LocalDateTime checkOutTime;
    @Schema(description = "总工时")
    private BigDecimal totalHours;
    @Schema(description = "考勤状态")
    private String status;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
