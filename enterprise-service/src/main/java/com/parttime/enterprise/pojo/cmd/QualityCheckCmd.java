package com.parttime.enterprise.pojo.cmd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "质量检查回调请求")
public class QualityCheckCmd {

    @Schema(description = "外部任务ID", required = true)
    private String externalTaskId;

    @Schema(description = "外部批次ID", required = true)
    private String externalBatchId;

    @Schema(description = "外部工人ID", required = true)
    private String externalWorkerId;

    @Schema(description = "是否通过", required = true)
    private Boolean passed;

    @Schema(description = "已完成数量")
    private Integer itemsCompleted;
}
