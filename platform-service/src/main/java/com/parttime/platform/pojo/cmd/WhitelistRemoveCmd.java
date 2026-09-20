package com.parttime.platform.pojo.cmd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class WhitelistRemoveCmd {
    @Schema(description = "记录ID")
    private Long id;
    @Schema(description = "操作人姓名")
    private String operatorName;
}
