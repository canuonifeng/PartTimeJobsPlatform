package com.parttime.platform.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "服务费率VO")
public class ServiceFeeRateVO {

    @Schema(description = "分类ID(空表示默认费率)")
    private Long categoryId;
    @Schema(description = "分类名称")
    private String categoryName;
    @Schema(description = "费率")
    private BigDecimal rate;
}
