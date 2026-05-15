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
    @Schema(description = "模板时段ID")
    private Long templateSlotId;
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
    @Schema(description = "班次状态: ASSIGNED-已分配, CHECKED_IN-已签到, CHECKED_OUT-已签退, COMPLETED-已完成, ABSENT-缺勤, CANCELLED-已取消")
    private String status;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
