package com.parttime.platform.pojo.cmd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class SystemConfigUpdateCmd {

    @Schema(description = "配置值")
    private String value;
}
