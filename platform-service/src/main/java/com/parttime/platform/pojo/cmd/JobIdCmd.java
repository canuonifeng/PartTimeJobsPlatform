package com.parttime.platform.pojo.cmd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class JobIdCmd {
    @Schema(description = "职位ID")
    private Long jobId;
}
