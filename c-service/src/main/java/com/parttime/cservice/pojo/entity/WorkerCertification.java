package com.parttime.cservice.pojo.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class WorkerCertification {

    @Schema(description = "ID")
    private Long id;
    @Schema(description = "工人ID")
    private Long workerId;
    @Schema(description = "认证ID")
    private Long certificationId;
    @Schema(description = "状态 ACTIVE/EXPIRED")
    private String status;
    @Schema(description = "获得时间")
    private LocalDateTime grantedAt;
    @Schema(description = "过期时间，NULL=永久")
    private LocalDateTime expiresAt;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
