package com.parttime.enterprise.pojo.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class ScheduleTemplateSlot {

    @Schema(description = "时段ID")
    private Long id;
    @Schema(description = "模板ID")
    private Long templateId;
    @Schema(description = "星期几(1-7)")
    private Integer dayOfWeek;
    @Schema(description = "开始时间")
    private LocalTime startTime;
    @Schema(description = "结束时间")
    private LocalTime endTime;
    @Schema(description = "最大工人数")
    private Integer maxWorkers;
    @Schema(description = "打卡纬度")
    private BigDecimal locationLat;
    @Schema(description = "打卡经度")
    private BigDecimal locationLng;
    @Schema(description = "打卡半径(米)")
    private Integer locationRadius;
    @Schema(description = "打卡地点名称")
    private String locationName;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
