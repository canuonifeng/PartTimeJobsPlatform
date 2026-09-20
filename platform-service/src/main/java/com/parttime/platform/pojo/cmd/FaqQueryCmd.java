package com.parttime.platform.pojo.cmd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class FaqQueryCmd {
    @Schema(description = "关键词")
    private String keyword;
    @Schema(description = "分类")
    private String category;
    @Schema(description = "状态")
    private String status;
}
