package com.parttime.enterprise.pojo.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class EnterpriseBalanceVO {
    private BigDecimal balance;
    private BigDecimal creditLimit;
    private BigDecimal totalTopUp;
    private BigDecimal totalSpent;
    private BigDecimal usableBalance;   // balance + creditLimit
}
