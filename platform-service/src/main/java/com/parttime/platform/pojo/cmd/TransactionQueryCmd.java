package com.parttime.platform.pojo.cmd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class TransactionQueryCmd {
    @Schema(description = "交易类型")
    private String type;
    @Schema(description = "状态")
    private String status;
    @Schema(description = "关键词")
    private String keyword;
    @Schema(description = "企业ID")
    private Long companyId;
    @Schema(description = "开始日期 yyyy-MM-dd")
    private String startDate;
    @Schema(description = "结束日期 yyyy-MM-dd")
    private String endDate;
}
