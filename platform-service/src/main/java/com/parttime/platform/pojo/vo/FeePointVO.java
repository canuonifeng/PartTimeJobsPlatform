package com.parttime.platform.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "每日服务费点")
public class FeePointVO {
    @Schema(description = "日期")
    private String date;
    @Schema(description = "服务费金额")
    private BigDecimal amount;
}
