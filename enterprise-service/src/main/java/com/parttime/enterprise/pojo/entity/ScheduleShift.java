package com.parttime.enterprise.pojo.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class ScheduleShift {

    @Schema(description = "班次ID")
    private Long id;
    @Schema(description = "岗位ID")
    private Long jobId;
    @Schema(description = "企业ID")
    private Long companyId;
    @Schema(description = "报名ID快照")
    private Long applicationId;
    @Schema(description = "薪资类型快照")
    private String salaryType;
    @Schema(description = "薪资金额快照")
    private BigDecimal salaryAmount;
    @Schema(description = "薪资币种快照")
    private String salaryCurrency;
    @Schema(description = "工人ID")
    private Long workerId;
    @Schema(description = "班次日期")
    private LocalDate shiftDate;
    @Schema(description = "开始时间")
    private LocalTime startTime;
    @Schema(description = "结束时间")
    private LocalTime endTime;
    @Schema(description = "打卡纬度")
    private BigDecimal locationLat;
    @Schema(description = "打卡经度")
    private BigDecimal locationLng;
    @Schema(description = "打卡半径(米)")
    private Integer locationRadius;
    @Schema(description = "打卡地点名称")
    private String locationName;
    @Schema(description = "联系人姓名快照")
    private String contactName;
    @Schema(description = "联系人电话快照")
    private String contactPhone;
    @Schema(description = "班次状态: SCHEDULED-待上岗, ON_DUTY-工作中, COMPLETED-已完成, ABSENT-缺勤, LATE-迟到, EARLY_LEAVE-早退, LATE_EARLY_LEAVE-迟到并早退, CANCELLED-已取消")
    private String status;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
