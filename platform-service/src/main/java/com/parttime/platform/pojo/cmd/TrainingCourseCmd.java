package com.parttime.platform.pojo.cmd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class TrainingCourseCmd {

    @Schema(description = "课程ID，更新时必填")
    private Long id;
    @Schema(description = "关联技能认证ID")
    private Long certificationId;
    @Schema(description = "课程标题")
    private String title;
    @Schema(description = "课程摘要")
    private String summary;
    @Schema(description = "排序号")
    private Integer sortOrder;
}
