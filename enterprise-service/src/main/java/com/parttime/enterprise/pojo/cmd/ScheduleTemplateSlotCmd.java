package com.parttime.enterprise.pojo.cmd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalTime;

@Data
public class ScheduleTemplateSlotCmd {

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
}
