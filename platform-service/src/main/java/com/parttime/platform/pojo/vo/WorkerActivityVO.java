package com.parttime.platform.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "工人活跃度排名项")
public class WorkerActivityVO {

    @Schema(description = "排名")
    private Integer rank;
    @Schema(description = "工人ID")
    private Long workerId;
    @Schema(description = "工人姓名")
    private String workerName;
    @Schema(description = "工人手机号")
    private String workerPhone;
    @Schema(description = "完成工作数")
    private Long completedJobs;
    @Schema(description = "累计收入")
    private BigDecimal totalEarnings;
    @Schema(description = "活跃度: HIGH/MEDIUM/LOW")
    private String activityLevel;
}
