package com.parttime.enterprise.pojo.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class JobApplication {

    @Schema(description = "申请ID")
    private Long id;
    @Schema(description = "岗位ID")
    private Long jobId;
    @Schema(description = "工人ID")
    private Long workerId;
    @Schema(description = "申请状态: PENDING-待处理, ACCEPTED-已通过, REJECTED-已拒绝")
    private String status;
    @Schema(description = "申请时间")
    private LocalDateTime appliedAt;
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
