package com.parttime.platform.pojo.cmd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "热门推荐添加命令")
public class HotRecommendationCmd {

    @Schema(description = "职位ID")
    private Long jobId;
}
