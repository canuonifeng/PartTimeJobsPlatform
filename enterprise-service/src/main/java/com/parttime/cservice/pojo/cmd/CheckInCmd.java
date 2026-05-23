package com.parttime.cservice.pojo.cmd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CheckInCmd {

    @Schema(description = "班次ID")
    private Long shiftId;
    @Schema(description = "纬度")
    private BigDecimal lat;
    @Schema(description = "经度")
    private BigDecimal lng;
}
