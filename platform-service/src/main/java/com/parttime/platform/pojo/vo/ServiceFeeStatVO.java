package com.parttime.platform.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "服务费统计")
public class ServiceFeeStatVO {
    @Schema(description = "累计服务费")
    private BigDecimal totalServiceFee;
    @Schema(description = "今日服务费")
    private BigDecimal todayServiceFee;
    @Schema(description = "本周服务费")
    private BigDecimal weekServiceFee;
    @Schema(description = "本月服务费")
    private BigDecimal monthServiceFee;
    @Schema(description = "每日服务费趋势")
    private List<FeePointVO> trend;
    @Schema(description = "分类服务费")
    private List<CategoryFeeVO> byCategory;
}
