package com.parttime.enterprise.pojo.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Job {

    @Schema(description = "岗位ID")
    private Long id;
    @Schema(description = "企业ID")
    private Long companyId;
    @Schema(description = "岗位标题")
    private String title;
    @Schema(description = "岗位描述")
    private String description;
    @Schema(description = "工作地点")
    private String location;
    @Schema(description = "岗位分类ID")
    private Long categoryId;
    @Schema(description = "招聘人数")
    private Integer headcount;
    @Schema(description = "岗位状态: DRAFT-草稿, PUBLISHED-已发布, CLOSED-已关闭")
    private String status;
    @Schema(description = "报名截止时间")
    private LocalDateTime deadline;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
