package com.parttime.enterprise.pojo.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AttendanceRecord {

    @Schema(description = "考勤记录ID")
    private Long id;
    @Schema(description = "班次ID")
    private Long shiftId;
    @Schema(description = "岗位ID")
    private Long jobId;
    @Schema(description = "企业ID")
    private Long companyId;
    @Schema(description = "工人ID")
    private Long workerId;
    @Schema(description = "签到时间")
    private LocalDateTime checkInTime;
    @Schema(description = "签到纬度")
    private BigDecimal checkInLat;
    @Schema(description = "签到经度")
    private BigDecimal checkInLng;
    @Schema(description = "签退时间")
    private LocalDateTime checkOutTime;
    @Schema(description = "签退纬度")
    private BigDecimal checkOutLat;
    @Schema(description = "签退经度")
    private BigDecimal checkOutLng;
    @Schema(description = "总工时")
    private BigDecimal totalHours;
    @Schema(description = "排班薪资")
    private BigDecimal scheduledPay;
    @Schema(description = "应付薪资")
    private BigDecimal payablePay;
    @Schema(description = "是否已发放")
    private Boolean isPaid;
    @Schema(description = "结算计算时间")
    private LocalDateTime calculatedAt;
    @Schema(description = "考勤状态: NORMAL-正常, LATE-迟到, EARLY_LEAVE-早退, ABSENT-缺勤")
    private String status;
    @Schema(description = "备注")
    private String remark;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
