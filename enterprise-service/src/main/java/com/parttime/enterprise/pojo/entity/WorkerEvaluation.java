package com.parttime.enterprise.pojo.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class WorkerEvaluation {

    @Schema(description = "评价ID")
    private Long id;
    @Schema(description = "企业ID")
    private Long companyId;
    @Schema(description = "岗位ID")
    private Long jobId;
    @Schema(description = "工人ID")
    private Long workerId;
    @Schema(description = "评分(1-5)")
    private Integer rating;
    @Schema(description = "评价内容")
    private String comment;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
