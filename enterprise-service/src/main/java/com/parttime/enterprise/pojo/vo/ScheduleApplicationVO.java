package com.parttime.enterprise.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import com.parttime.enterprise.enums.ApplicationStatus;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Data
public class ScheduleApplicationVO {

    @Schema(description = "申请ID")
    private Long id;
    @Schema(description = "申请ID(同id)")
    private Long applicationId;
    @Schema(description = "排班ID")
    private Long scheduleId;
    @Schema(description = "岗位ID")
    private Long jobId;
    @Schema(description = "岗位标题")
    private String jobTitle;
    @Schema(description = "工人ID")
    private Long workerId;
    @Schema(description = "工人姓名")
    private String workerName;
    @Schema(description = "工人手机号")
    private String workerPhone;
    @Schema(description = "年龄")
    private Integer workerAge;
    @Schema(description = "申请状态")
    private ApplicationStatus status;
    @Schema(description = "申请时间")
    private LocalDateTime appliedAt;
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
    @Schema(description = "排班日期")
    private LocalDate scheduleDate;
    @Schema(description = "开始时间")
    private LocalTime startTime;
    @Schema(description = "结束时间")
    private LocalTime endTime;
}
