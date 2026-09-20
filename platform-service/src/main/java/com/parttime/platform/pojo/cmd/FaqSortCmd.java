package com.parttime.platform.pojo.cmd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class FaqSortCmd {
    @Schema(description = "FAQ ID")
    private Long id;
    @Schema(description = "排序值")
    private Integer sortOrder;
}
