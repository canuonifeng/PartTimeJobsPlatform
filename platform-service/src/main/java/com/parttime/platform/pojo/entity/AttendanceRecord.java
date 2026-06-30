package com.parttime.platform.pojo.entity;

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
    @Schema(description = "岗位名称")
    private String jobTitle;
    @Schema(description = "企业ID")
    private Long companyId;
    @Schema(description = "企业名称")
    private String companyName;
    @Schema(description = "工人ID")
    private Long workerId;
    @Schema(description = "工人姓名")
    private String workerName;
    @Schema(description = "工人电话")
    private String workerPhone;
    @Schema(description = "签到时间")
    private LocalDateTime checkInTime;
    @Schema(description = "签退时间")
    private LocalDateTime checkOutTime;
    @Schema(description = "总工时")
    private BigDecimal totalHours;
    @Schema(description = "排班薪资")
    private BigDecimal scheduledPay;
    @Schema(description = "应付薪资")
    private BigDecimal payablePay;
    @Schema(description = "结算状态")
    private String settlementStatus;
    @Schema(description = "考勤状态")
    private String status;
    @Schema(description = "备注")
    private String remark;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
