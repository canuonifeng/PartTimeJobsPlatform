package com.parttime.platform.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class JobReportVO {

    @Schema(description = "举报ID")
    private Long id;
    @Schema(description = "岗位ID")
    private Long jobId;
    @Schema(description = "举报人ID")
    private Long reporterId;
    @Schema(description = "举报原因")
    private String reason;
    @Schema(description = "举报描述")
    private String description;
    @Schema(description = "处理状态")
    private String status;
    @Schema(description = "审核人ID")
    private String reviewerId;
    @Schema(description = "审核备注")
    private String reviewRemark;
    @Schema(description = "审核时间")
    private LocalDateTime reviewedAt;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
