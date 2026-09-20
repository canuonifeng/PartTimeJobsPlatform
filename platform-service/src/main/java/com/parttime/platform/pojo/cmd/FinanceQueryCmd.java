package com.parttime.platform.pojo.cmd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class FinanceQueryCmd {
    @Schema(description = "开始日期 yyyy-MM-dd")
    private String startDate;
    @Schema(description = "结束日期 yyyy-MM-dd")
    private String endDate;
}
