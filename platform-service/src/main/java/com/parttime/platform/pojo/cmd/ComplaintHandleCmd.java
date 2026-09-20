package com.parttime.platform.pojo.cmd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ComplaintHandleCmd {
    @Schema(description = "工单ID")
    private Long id;
    @Schema(description = "处理结果")
    private String handleResult;
    @Schema(description = "处理人姓名")
    private String handlerName;
}
