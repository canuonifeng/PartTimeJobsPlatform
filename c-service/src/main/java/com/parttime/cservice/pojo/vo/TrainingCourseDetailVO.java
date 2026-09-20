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
    @Schema(description = "课程内容（Markdown/HTML）")
    private String content;
    @Schema(description = "及格分（百分制）")
    private Integer passScore;
    @Schema(description = "考试题目（不含答案）")
    private List<ExamQuestionVO> questions;
    @Schema(description = "我的学习状态：NOT_STARTED/IN_PROGRESS/COMPLETED/FAILED")
    private String myStatus;
    @Schema(description = "我的最近一次得分")
    private Integer myScore;
    @Schema(description = "是否已获得该课程关联认证")
    private Boolean certified;
}
