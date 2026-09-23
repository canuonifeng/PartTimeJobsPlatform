package com.parttime.enterprise.pojo.cmd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class AnnotationBatchListCmd {

    @Schema(description = "岗位ID")
    private Long jobId;
}
