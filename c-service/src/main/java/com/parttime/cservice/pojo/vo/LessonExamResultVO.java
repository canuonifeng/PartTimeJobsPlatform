package com.parttime.cservice.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class LessonExamResultVO {

    @Schema(description = "是否通过")
    private Boolean passed;
    @Schema(description = "本次得分")
    private Integer score;
    @Schema(description = "及格分")
    private Integer passScore;
    @Schema(description = "试卷总分")
    private Integer totalScore;
    @Schema(description = "通过时的回顾快照")
    private Object snapshot;
}
