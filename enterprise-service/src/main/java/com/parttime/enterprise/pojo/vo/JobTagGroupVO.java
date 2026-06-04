package com.parttime.enterprise.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class JobTagGroupVO {

    @Schema(description = "标签组ID")
    private Long id;
    @Schema(description = "标签组名称")
    private String name;
    @Schema(description = "标签组编码")
    private String code;
    @Schema(description = "排序号")
    private Integer sortOrder;
    @Schema(description = "标签组状态")
    private String status;
    @Schema(description = "标签列表")
    private List<JobTagVO> tags;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
