package com.parttime.platform.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "工人活跃度趋势项")
public class WorkerTrendVO {

    @Schema(description = "日期")
    private String date;
    @Schema(description = "新增工人数")
    private Long newWorkers;
    @Schema(description = "活跃工人数")
    private Long activeWorkers;
    @Schema(description = "报名数")
    private Long applications;
}
