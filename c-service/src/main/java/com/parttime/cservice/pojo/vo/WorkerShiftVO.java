package com.parttime.cservice.pojo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class WorkerShiftVO {

    @Schema(description = "班次ID")
    private Long id;
    @Schema(description = "岗位ID")
    private Long jobId;
    @Schema(description = "岗位标题")
    private String jobTitle;
    @Schema(description = "岗位地点")
    private String location;
    @Schema(description = "班次日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;
    @Schema(description = "开始时间")
    private String startTime;
    @Schema(description = "结束时间")
    private String endTime;
    @Schema(description = "班次状态")
    private String status;
    @Schema(description = "签到时间")
    private LocalDateTime checkInTime;
    @Schema(description = "签退时间")
    private LocalDateTime checkOutTime;
    @Schema(description = "工时")
    private BigDecimal workHours;
    @Schema(description = "补卡状态: PENDING/APPROVED/REJECTED/null")
    private String correctionStatus;
    @Schema(description = "打卡纬度")
    private BigDecimal locationLat;
    @Schema(description = "打卡经度")
    private BigDecimal locationLng;
    @Schema(description = "打卡半径(米)")
    private Integer locationRadius;
    @Schema(description = "打卡地点名称")
    private String locationName;
}
