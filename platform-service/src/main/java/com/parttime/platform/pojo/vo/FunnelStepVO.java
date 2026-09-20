package com.parttime.platform.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "漏斗步骤")
public class FunnelStepVO {

    @Schema(description = "步骤名称")
    private String name;
    @Schema(description = "数量")
    private Long count;
}
