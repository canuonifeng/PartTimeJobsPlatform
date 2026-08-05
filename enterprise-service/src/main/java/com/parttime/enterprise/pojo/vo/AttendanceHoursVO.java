package com.parttime.enterprise.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class AttendanceHoursVO {

    @Schema(description = "考勤记录ID")
    private Long id;
    @Schema(description = "工人ID")
    private Long workerId;
    @Schema(description = "工人姓名")
    private String workerName;
    @Schema(description = "年龄")
    private Integer workerAge;
    @Schema(description = "岗位ID")
    private Long jobId;
    @Schema(description = "岗位名称")
    private String jobTitle;
    @Schema(description = "排班日期")
    private LocalDate shiftDate;
    @Schema(description = "开始时间")
    private LocalTime startTime;
    @Schema(description = "结束时间")
    private LocalTime endTime;
    @Schema(description = "薪资类型: HOURLY/DAILY/PER_SHIFT")
    private String salaryType;
    @Schema(description = "薪资标准金额")
    private BigDecimal salaryAmount;
    @Schema(description = "总工时")
    private BigDecimal totalHours;
    @Schema(description = "排班薪资")
    private BigDecimal scheduledPay;
    @Schema(description = "应付薪资")
    private BigDecimal payablePay;
    @Schema(description = "结算状态: UNPAID/PAYING/PAID")
    private String settlementStatus;
    @Schema(description = "签到时间")
    private LocalDateTime checkInTime;
    @Schema(description = "签退时间")
    private LocalDateTime checkOutTime;
}
