package com.parttime.enterprise.pojo.cmd;

import lombok.Data;
import java.time.LocalDate;

@Data
public class PayrollBatchCmd {

    private Long companyId;
    private String name;
    private LocalDate periodStart;
    private LocalDate periodEnd;
}
