package com.parttime.cservice.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class HomeStatsVO {

    @Schema(description = "本月工时")
    private BigDecimal monthHours;
    @Schema(description = "本月收入")
    private BigDecimal monthIncome;
    @Schema(description = "出勤天数")
    private Integer attendanceDays;
}
