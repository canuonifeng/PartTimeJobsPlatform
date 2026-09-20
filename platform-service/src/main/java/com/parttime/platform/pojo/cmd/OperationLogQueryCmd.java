package com.parttime.platform.pojo.cmd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "操作日志查询命令")
public class OperationLogQueryCmd {

    @Schema(description = "模块")
    private String module;
    @Schema(description = "操作人姓名")
    private String operatorName;
    @Schema(description = "操作类型")
    private String operationType;
}
