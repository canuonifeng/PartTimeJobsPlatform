package com.parttime.platform.pojo.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ScheduleApplication {

    @Schema(description = "申请ID")
    private Long id;
    @Schema(description = "排班ID")
    private Long scheduleId;
    @Schema(description = "岗位ID")
    private Long jobId;
    @Schema(description = "岗位名称")
    private String jobTitle;
    @Schema(description = "企业ID")
    private Long companyId;
    @Schema(description = "企业名称")
    private String companyName;
    @Schema(description = "工人ID")
    private Long workerId;
    @Schema(description = "工人姓名")
    private String workerName;
    @Schema(description = "工人电话")
    private String workerPhone;
    @Schema(description = "申请状态")
    private String status;
    @Schema(description = "申请时间")
    private LocalDateTime appliedAt;
    @Schema(description = "审核时间")
    private LocalDateTime reviewedAt;
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
