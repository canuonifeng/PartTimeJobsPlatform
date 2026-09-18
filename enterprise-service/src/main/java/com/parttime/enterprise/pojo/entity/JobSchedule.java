package com.parttime.enterprise.pojo.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class JobSchedule {

    @Schema(description = "排班ID")
    private Long id;
    @Schema(description = "岗位ID")
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
    @Schema(description = "联系人姓名快照")
    private String contactName;
    @Schema(description = "联系人电话快照")
    private String contactPhone;
    @Schema(description = "状态 ACTIVE/CANCELLED")
    private String status;
    @Schema(description = "总数量")
    private Integer totalItems;
    @Schema(description = "外部批次ID")
    private String externalBatchId;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
