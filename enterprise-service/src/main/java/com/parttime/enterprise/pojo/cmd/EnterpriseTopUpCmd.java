package com.parttime.enterprise.pojo.cmd;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class EnterpriseTopUpCmd {
    private BigDecimal amount;
}
