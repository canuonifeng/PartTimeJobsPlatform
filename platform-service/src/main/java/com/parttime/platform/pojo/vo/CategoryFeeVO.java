package com.parttime.platform.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "分类服务费")
public class CategoryFeeVO {
    @Schema(description = "分类")
    private String category;
    @Schema(description = "服务费金额")
    private BigDecimal amount;
}
