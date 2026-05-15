package com.parttime.enterprise.pojo.cmd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import com.parttime.enterprise.enums.JobRateType;
import java.math.BigDecimal;

@Data
public class JobRateCmd {

    @Schema(description = "薪资类型")
    private JobRateType type;
    @Schema(description = "金额")
    private BigDecimal amount;
    @Schema(description = "币种")
    private String currency;
    @Schema(description = "薪资规则描述")
    private String rules;
}
