package com.parttime.platform.pojo.cmd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "活动状态切换命令")
public class ActivityToggleCmd {

    @Schema(description = "活动ID")
    private Long id;
    @Schema(description = "目标状态: DRAFT, PUBLISHED, ENDED")
    private String status;
}
