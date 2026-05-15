package com.parttime.enterprise.pojo.cmd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDate;

@Data
public class PayrollBatchCmd {

    @Schema(description = "企业ID")
    private Long companyId;
    @Schema(description = "批次名称")
    private String name;
    @Schema(description = "计薪开始日期")
    private LocalDate periodStart;
    @Schema(description = "计薪结束日期")
    private LocalDate periodEnd;
}
