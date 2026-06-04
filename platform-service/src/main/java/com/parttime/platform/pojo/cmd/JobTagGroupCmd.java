package com.parttime.platform.pojo.cmd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class JobTagGroupCmd {

    @Schema(description = "标签组名称")
    private String name;
    @Schema(description = "标签组编码")
    private String code;
    @Schema(description = "排序号")
    private Integer sortOrder;
    @Schema(description = "标签组状态")
    private String status;
}
