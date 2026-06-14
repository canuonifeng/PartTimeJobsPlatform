package com.parttime.platform.pojo.cmd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class IdCmd {
    @Schema(description = "ID")
    private Long id;
}
