package com.parttime.cservice.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class TrainingCourseDetailVO {

    @Schema(description = "课程ID")
    private Long id;
    @Schema(description = "关联技能认证ID")
    private Long certificationId;
    @Schema(description = "认证名称")
    private String certificationName;
    @Schema(description = "课程标题")
    private String title;
    @Schema(description = "课程摘要")
    private String summary;
    @Schema(description = "是否已获得该课程关联认证")
    private Boolean certified;
    @Schema(description = "该课程 PUBLISHED 课时列表，按 sort_order 排序")
    private List<TrainingLessonVO> lessons;
}
