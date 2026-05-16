package com.parttime.enterprise.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import com.parttime.enterprise.enums.ApplicationStatus;
import java.time.LocalDateTime;

@Data
public class JobApplicationVO {

    @Schema(description = "申请ID")
    private Long id;
    @Schema(description = "岗位ID")
    private Long jobId;
    @Schema(description = "岗位标题")
    private String jobTitle;
    @Schema(description = "工人ID")
    private Long workerId;
    @Schema(description = "工人姓名")
    private String workerName;
    @Schema(description = "申请状态")
    private ApplicationStatus status;
    @Schema(description = "申请时间")
    private LocalDateTime appliedAt;
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
