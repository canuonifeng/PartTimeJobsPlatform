package com.parttime.cservice.pojo.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class WorkerRealNameAuth {
    @Schema(description = "ID")
    private Long id;
    @Schema(description = "工人ID")
    private Long workerId;
    @Schema(description = "真实姓名")
    private String realName;
    @Schema(description = "身份证号")
    private String idCardNo;
    @Schema(description = "身份证正面URL")
    private String idCardFrontUrl;
    @Schema(description = "身份证反面URL")
    private String idCardBackUrl;
    @Schema(description = "状态: PENDING/APPROVED/REJECTED")
    private String status;
    @Schema(description = "拒绝原因")
    private String rejectReason;
    private LocalDateTime submittedAt;
    private LocalDateTime reviewedAt;
    private Long reviewerId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
