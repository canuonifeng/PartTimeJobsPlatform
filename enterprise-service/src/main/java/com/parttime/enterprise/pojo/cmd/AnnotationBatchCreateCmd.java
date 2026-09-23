package com.parttime.enterprise.pojo.cmd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class AnnotationBatchCreateCmd {

    @Schema(description = "岗位ID")
    private Long jobId;
    @Schema(description = "批次编码")
    private String batchCode;
    @Schema(description = "总数量")
    private Integer totalItems;
    @Schema(description = "外部批次ID")
    private String externalBatchId;
}
