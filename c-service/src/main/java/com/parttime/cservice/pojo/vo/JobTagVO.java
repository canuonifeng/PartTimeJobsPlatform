package com.parttime.cservice.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class JobTagVO {

    @Schema(description = "标签ID")
    private Long id;
    @Schema(description = "岗位ID")
    private Long jobId;
    @Schema(description = "标签组ID")
    private Long groupId;
    @Schema(description = "标签组名称")
    private String groupName;
    @Schema(description = "标签名称")
    private String name;
    @Schema(description = "标签编码")
    private String code;
    @Schema(description = "排序号")
    private Integer sortOrder;
    @Schema(description = "标签状态")
    private String status;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
