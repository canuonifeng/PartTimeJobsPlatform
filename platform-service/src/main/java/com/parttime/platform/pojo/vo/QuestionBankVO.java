package com.parttime.platform.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class QuestionBankVO {

    @Schema(description = "题库ID")
    private Long id;
    @Schema(description = "题库名称")
    private String name;
    @Schema(description = "题库描述")
    private String description;
    @Schema(description = "状态 ACTIVE/DISABLED")
    private String status;
    @Schema(description = "题目数量")
    private Integer count;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
