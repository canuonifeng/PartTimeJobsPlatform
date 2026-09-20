package com.parttime.platform.pojo.cmd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class JobTopCmd {
    @Schema(description = "职位ID")
    private Long id;
    @Schema(description = "是否置顶")
    private Boolean isTop;
}
