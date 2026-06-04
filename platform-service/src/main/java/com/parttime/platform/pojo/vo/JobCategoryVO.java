package com.parttime.platform.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class JobCategoryVO {

    @Schema(description = "分类ID")
    private Long id;
    @Schema(description = "分类名称")
    private String name;
    @Schema(description = "父分类ID")
    private Long parentId;
    @Schema(description = "排序号")
    private Integer sortOrder;
    @Schema(description = "分类状态")
    private String status;
    @Schema(description = "子分类列表")
    private List<JobCategoryVO> children;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
