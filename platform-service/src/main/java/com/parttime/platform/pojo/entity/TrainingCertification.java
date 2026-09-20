package com.parttime.platform.pojo.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TrainingCertification {

    @Schema(description = "认证ID")
    private Long id;
    @Schema(description = "认证名称")
    private String name;
    @Schema(description = "认证编码")
    private String code;
    @Schema(description = "适用任务类型 WORK/ANNOTATION")
    private String taskType;
    @Schema(description = "认证描述")
    private String description;
    @Schema(description = "有效期天数，NULL=永久")
    private Integer validDays;
    @Schema(description = "状态 ACTIVE/DISABLED")
    private String status;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
