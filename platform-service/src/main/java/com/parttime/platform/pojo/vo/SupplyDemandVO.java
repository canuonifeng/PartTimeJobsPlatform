package com.parttime.platform.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "职位供需分析")
public class SupplyDemandVO {

    @Schema(description = "总需求(职位数)")
    private Long totalDemand;
    @Schema(description = "总供给(报名人数)")
    private Long totalSupply;
    @Schema(description = "匹配率")
    private BigDecimal matchingRate;
    @Schema(description = "分类供需")
    private List<CategorySupplyVO> categories;
}
