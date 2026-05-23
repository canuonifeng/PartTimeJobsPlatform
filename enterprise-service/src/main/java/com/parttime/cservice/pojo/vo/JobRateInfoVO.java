package com.parttime.cservice.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class JobRateInfoVO {
    @Schema(description = "薪资规则ID")
    private Long id;
    @Schema(description = "薪资类型")
    private String type;
    @Schema(description = "金额")
    private BigDecimal amount;
    @Schema(description = "币种")
    private String currency;
}
