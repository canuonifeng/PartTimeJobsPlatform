package com.parttime.platform.pojo.cmd;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class EnterpriseBalanceAdjustCmd {
    private Long companyId;
    private BigDecimal amount;
    private String description;
}
