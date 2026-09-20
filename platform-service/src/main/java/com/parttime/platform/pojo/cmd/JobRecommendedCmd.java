package com.parttime.platform.pojo.cmd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class JobRecommendedCmd {
    @Schema(description = "职位ID")
    private Long id;
    @Schema(description = "是否推荐")
    private Boolean isRecommended;
}
