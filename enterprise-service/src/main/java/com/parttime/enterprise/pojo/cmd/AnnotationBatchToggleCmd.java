package com.parttime.enterprise.pojo.cmd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class AnnotationBatchToggleCmd {

    @Schema(description = "批次ID")
    private Long id;
    @Schema(description = "状态 ACTIVE/CANCELLED")
    private String status;
}
