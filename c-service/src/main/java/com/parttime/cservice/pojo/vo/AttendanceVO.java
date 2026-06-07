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
    @Schema(description = "排班薪资")
    private BigDecimal scheduledPay;
    @Schema(description = "工资计算时间")
    private LocalDateTime calculatedAt;
    @Schema(description = "考勤状态")
    private String status;
    @Schema(description = "岗位标题")
    private String jobTitle;
    @Schema(description = "企业名称")
    private String companyName;
    @Schema(description = "工作地点")
    private String location;
    @Schema(description = "班次日期")
    private String shiftDate;
    @Schema(description = "班次开始时间")
    private String startTime;
    @Schema(description = "班次结束时间")
    private String endTime;
    @Schema(description = "应付薪资")
    private BigDecimal payablePay;
    @Schema(description = "结算状态")
    private String settlementStatus;
}
