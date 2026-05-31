package com.parttime.cservice.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class WorkerRealNameAuthVO {
    @Schema(description = "状态: NONE/PENDING/APPROVED/REJECTED")
    private String status;
    @Schema(description = "真实姓名")
    private String realName;
    @Schema(description = "身份证号(部分掩码)")
    private String idCardNoMasked;
    @Schema(description = "拒绝原因")
    private String rejectReason;
    private LocalDateTime submittedAt;
    private LocalDateTime reviewedAt;
}
