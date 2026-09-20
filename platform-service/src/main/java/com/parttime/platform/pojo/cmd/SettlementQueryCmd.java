package com.parttime.platform.pojo.cmd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class SettlementQueryCmd {
    @Schema(description = "状态")
    private String status;
    @Schema(description = "企业ID")
    private Long companyId;
    @Schema(description = "关键词")
    private String keyword;
}
