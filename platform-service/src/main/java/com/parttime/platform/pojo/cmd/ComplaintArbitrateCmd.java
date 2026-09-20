package com.parttime.platform.pojo.cmd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ComplaintArbitrateCmd {
    @Schema(description = "工单ID")
    private Long id;
    @Schema(description = "仲裁结论")
    private String conclusion;
    @Schema(description = "处理人姓名")
    private String handlerName;
}
