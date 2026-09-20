package com.parttime.platform.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "企业活跃度排名项")
public class EnterpriseActivityVO {

    @Schema(description = "排名")
    private Integer rank;
    @Schema(description = "企业ID")
    private Long enterpriseId;
    @Schema(description = "企业名称")
    private String enterpriseName;
    @Schema(description = "发布职位数")
    private Long jobCount;
    @Schema(description = "报名总数")
    private Long applicationCount;
    @Schema(description = "累计结算金额")
    private BigDecimal totalSettlement;
    @Schema(description = "活跃度: HIGH/MEDIUM/LOW")
    private String activityLevel;
}
