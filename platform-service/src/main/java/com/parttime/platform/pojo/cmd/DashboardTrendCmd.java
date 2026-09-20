package com.parttime.platform.pojo.cmd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class DashboardTrendCmd {
    @Schema(description = "统计近 N 天，默认 7")
    private Integer days;
}
