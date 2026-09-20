package com.parttime.platform.pojo.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Faq {

    @Schema(description = "FAQ ID")
    private Long id;
    @Schema(description = "问题")
    private String question;
    @Schema(description = "答案")
    private String answer;
    @Schema(description = "分类")
    private String category;
    @Schema(description = "排序")
    private Integer sortOrder;
    @Schema(description = "浏览次数")
    private Integer viewCount;
    @Schema(description = "有帮助计数")
    private Integer helpfulCount;
    @Schema(description = "无帮助计数")
    private Integer notHelpfulCount;
    @Schema(description = "状态: ACTIVE, INACTIVE")
    private String status;
    @Schema(description = "操作人ID")
    private Long operatorId;
    @Schema(description = "操作人姓名")
    private String operatorName;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
