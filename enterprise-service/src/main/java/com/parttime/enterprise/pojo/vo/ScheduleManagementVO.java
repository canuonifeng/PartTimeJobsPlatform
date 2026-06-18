package com.parttime.enterprise.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class ScheduleManagementVO {
    @Schema(description = "班次ID")
    private Long id;
    @Schema(description = "岗位ID")
    private Long jobId;
    @Schema(description = "岗位名称")
    private String jobTitle;
    @Schema(description = "班次名称")
    private String scheduleName;
    @Schema(description = "班次日期")
    private LocalDate scheduleDate;
    @Schema(description = "开始时间")
    private LocalTime startTime;
    @Schema(description = "结束时间")
    private LocalTime endTime;
    @Schema(description = "工作地点")
    private String location;
    @Schema(description = "联系人姓名")
    private String contactName;
    @Schema(description = "联系人电话")
    private String contactPhone;
    @Schema(description = "招聘人数")
    private Integer slotsAvailable;
    @Schema(description = "总报名人数")
    private Integer applicationCount;
    @Schema(description = "待审核人数")
    private Integer pendingCount;
    @Schema(description = "已通过人数")
    private Integer acceptedCount;
    @Schema(description = "已拒绝人数")
    private Integer rejectedCount;
    @Schema(description = "剩余名额")
    private Integer remainingSlots;
    @Schema(description = "已生成排班数")
    private Integer shiftCount;
    @Schema(description = "异常考勤数")
    private Integer exceptionCount;
    @Schema(description = "已结算数")
    private Integer settledCount;
    @Schema(description = "待补卡数")
    private Integer correctionPendingCount;
    @Schema(description = "状态")
    private String status;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
