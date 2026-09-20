package com.parttime.cservice.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class WorkerCertificationVO {

    @Schema(description = "认证ID")
    private Long certificationId;
    @Schema(description = "认证名称")
    private String name;
    @Schema(description = "认证编码")
    private String code;
    @Schema(description = "适用任务类型")
    private String taskType;
    @Schema(description = "状态 ACTIVE/EXPIRED")
    private String status;
    @Schema(description = "获得时间")
    private LocalDateTime grantedAt;
    @Schema(description = "过期时间，NULL=永久")
    private LocalDateTime expiresAt;
}
