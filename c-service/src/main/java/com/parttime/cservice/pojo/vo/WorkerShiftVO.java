package com.parttime.cservice.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class WorkerShiftVO {

    @Schema(description = "班次ID")
    private Long shiftId;
    @Schema(description = "岗位ID")
    private Long jobId;
    @Schema(description = "岗位标题")
    private String jobTitle;
    @Schema(description = "岗位地点")
    private String jobLocation;
    @Schema(description = "班次日期")
    private LocalDate shiftDate;
    @Schema(description = "开始时间")
    private LocalTime startTime;
    @Schema(description = "结束时间")
    private LocalTime endTime;
    @Schema(description = "班次状态")
    private String status;
    @Schema(description = "打卡纬度")
    private BigDecimal locationLat;
    @Schema(description = "打卡经度")
    private BigDecimal locationLng;
    @Schema(description = "打卡半径(米)")
    private Integer locationRadius;
    @Schema(description = "打卡地点名称")
    private String locationName;
}
