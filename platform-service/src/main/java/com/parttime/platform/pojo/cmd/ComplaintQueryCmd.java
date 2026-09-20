package com.parttime.platform.pojo.cmd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ComplaintQueryCmd {
    @Schema(description = "状态: PENDING/PROCESSING/RESOLVED/CLOSED")
    private String status;
    @Schema(description = "投诉类型: PAYMENT/SCHEDULE/BEHAVIOR/SERVICE/OTHER")
    private String complaintType;
    @Schema(description = "投诉人类型: WORKER/ENTERPRISE")
    private String complainantType;
    @Schema(description = "关键词")
    private String keyword;
}
