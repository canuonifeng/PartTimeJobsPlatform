package com.parttime.cservice.pojo.cmd;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class WithdrawalCmd {

    private BigDecimal amount;
}
