package com.parttime.cservice.pojo.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AttendanceCorrectionEntity {

    @Schema(description = "补卡申请ID")
    private Long id;
    @Schema(description = "班次ID")
    private Long shiftId;
    @Schema(description = "工人ID")
    private Long workerId;
    @Schema(description = "补卡原因")
    private String reason;
    @Schema(description = "状态: PENDING/APPROVED/REJECTED")
    private String status;
    @Schema(description = "拒绝原因")
    private String rejectReason;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
    @Schema(description = "处理时间")
    private LocalDateTime processedAt;
    @Schema(description = "处理人ID")
    private Long processorId;

    public AttendanceCorrectionEntity() {}
}
