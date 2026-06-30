package com.parttime.platform.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class JobScheduleVO {
    @Schema(description = "排班ID")
    private Long id;
    @Schema(description = "职位ID")
    private Long jobId;
    @Schema(description = "排班日期")
    private LocalDate scheduleDate;
    @Schema(description = "开始时间")
    private LocalTime startTime;
    @Schema(description = "结束时间")
    private LocalTime endTime;
    @Schema(description = "班次名称")
    private String scheduleName;
    @Schema(description = "可报名人数")
    private Integer slotsAvailable;
    @Schema(description = "已报名人数")
    private Integer applicationCount;
    @Schema(description = "状态")
    private String status;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
