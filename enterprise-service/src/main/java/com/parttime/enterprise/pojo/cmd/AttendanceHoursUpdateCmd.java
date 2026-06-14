package com.parttime.enterprise.pojo.cmd;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AttendanceHoursUpdateCmd {
    private Long id;
    private BigDecimal totalHours;
    private BigDecimal scheduledPay;
    private BigDecimal payablePay;
}
