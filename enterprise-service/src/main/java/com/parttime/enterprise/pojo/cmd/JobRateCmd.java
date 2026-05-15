package com.parttime.enterprise.pojo.cmd;

import lombok.Data;
import com.parttime.enterprise.enums.JobRateType;
import java.math.BigDecimal;

@Data
public class JobRateCmd {

    private JobRateType type;
    private BigDecimal amount;
    private String currency;
    private String rules;
}
