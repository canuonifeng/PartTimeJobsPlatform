package com.parttime.platform.pojo.cmd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class JobTagCmd {

    @Schema(description = "标签组ID")
    private Long groupId;
    @Schema(description = "标签名称")
    private String name;
    @Schema(description = "标签编码")
    private String code;
    @Schema(description = "排序号")
    private Integer sortOrder;
    @Schema(description = "标签状态")
    private String status;
}
