package com.parttime.platform.pojo.cmd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ToggleStatusCmd {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "目标状态：ACTIVE 启用 / DISABLED 停用")
    private String status;
}
