package com.parttime.enterprise.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class ScheduleShiftVO {

    @Schema(description = "班次ID")
    private Long id;
    @Schema(description = "岗位ID")
    private Long jobId;
    @Schema(description = "报名ID快照")
    private Long applicationId;
    @Schema(description = "薪资类型快照")
    private String salaryType;
    @Schema(description = "薪资金额快照")
    private BigDecimal salaryAmount;
    @Schema(description = "薪资币种快照")
    private String salaryCurrency;
    @Schema(description = "岗位标题")
    private String jobTitle;
    @Schema(description = "工人名称")
    private String workerName;
    @Schema(description = "工人手机号")
    private String workerPhone;
    @Schema(description = "工人性别")
    private String workerGender;
    @Schema(description = "年龄")
    private Integer workerAge;
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
    @Schema(description = "班次状态")
    private String status;
    @Schema(description = "签到状态: NO_CHECK_IN-未签到, CHECKED_IN-已签到, CHECKED_OUT-已签退")
    private String attendanceStatus;
    @Schema(description = "签到时间")
    private LocalDateTime checkInTime;
    @Schema(description = "签退时间")
    private LocalDateTime checkOutTime;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
