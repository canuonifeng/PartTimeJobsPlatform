package com.parttime.enterprise.pojo.cmd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "标注提交回调请求")
public class AnnotationSubmitCmd {

    @Schema(description = "外部任务ID", required = true)
    private String externalTaskId;

    @Schema(description = "外部批次ID", required = true)
    private String externalBatchId;

    @Schema(description = "外部工人ID", required = true)
    private String externalWorkerId;

    @Schema(description = "已完成数量", required = true)
    private Integer itemsCompleted;

    @Schema(description = "外部提交ID（防重放）")
    private String externalSubmissionId;
}
