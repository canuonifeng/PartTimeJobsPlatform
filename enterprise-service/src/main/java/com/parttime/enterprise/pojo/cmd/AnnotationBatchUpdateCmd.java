package com.parttime.enterprise.pojo.cmd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class AnnotationBatchUpdateCmd {

    @Schema(description = "批次ID")
    private Long id;
    @Schema(description = "批次编码")
    private String batchCode;
    @Schema(description = "总数量")
    private Integer totalItems;
    @Schema(description = "外部批次ID")
    private String externalBatchId;
}
