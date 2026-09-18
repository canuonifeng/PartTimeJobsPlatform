package com.parttime.enterprise.pojo.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ExternalWorkerMapping {

    @Schema(description = "ID")
    private Long id;
    @Schema(description = "工人ID")
    private Long workerId;
    @Schema(description = "外部系统工人ID")
    private String externalWorkerId;
    @Schema(description = "外部系统类型")
    private String externalSystemType;
    @Schema(description = "企业ID")
    private Long companyId;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
