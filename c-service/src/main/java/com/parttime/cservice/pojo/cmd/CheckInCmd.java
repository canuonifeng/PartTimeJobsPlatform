package com.parttime.cservice.pojo.cmd;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CheckInCmd {

    private Long shiftId;
    private BigDecimal lat;
    private BigDecimal lng;
}
