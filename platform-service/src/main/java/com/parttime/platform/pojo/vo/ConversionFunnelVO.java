package com.parttime.platform.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "转化漏斗分析")
public class ConversionFunnelVO {

    @Schema(description = "浏览职位数")
    private Long browseCount;
    @Schema(description = "查看详情数")
    private Long viewCount;
    @Schema(description = "报名申请数")
    private Long applyCount;
    @Schema(description = "审核通过数")
    private Long approvedCount;
    @Schema(description = "实际到岗数")
    private Long checkinCount;
    @Schema(description = "完成结算数")
    private Long settlementCount;
    @Schema(description = "漏斗步骤")
    private List<FunnelStepVO> steps;
}
