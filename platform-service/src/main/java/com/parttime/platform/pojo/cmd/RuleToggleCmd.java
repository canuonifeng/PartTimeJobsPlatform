package com.parttime.platform.pojo.cmd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class RuleToggleCmd {
    @Schema(description = "规则ID")
    private Long id;
    @Schema(description = "是否启用")
    private Boolean enabled;
}
