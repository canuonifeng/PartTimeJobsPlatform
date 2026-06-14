package com.parttime.enterprise.pojo.cmd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class JobCategoryCmd {

    @Schema(description = "分类ID")
    private Long id;
    @Schema(description = "分类名称")
    private String name;
    @Schema(description = "父分类ID")
    private Long parentId;
    @Schema(description = "排序号")
    private Integer sortOrder;
    @Schema(description = "状态：ACTIVE-启用，DISABLED-停用")
    private String status;
}
