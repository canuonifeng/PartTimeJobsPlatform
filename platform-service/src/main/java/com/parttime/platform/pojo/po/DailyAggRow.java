package com.parttime.platform.pojo.po;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DailyAggRow {
    private String date;
    private BigDecimal amount;
    private Long count;
}
