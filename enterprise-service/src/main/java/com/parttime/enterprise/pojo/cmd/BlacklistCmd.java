package com.parttime.enterprise.pojo.cmd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class BlacklistCmd {

    @Schema(description = "企业ID")
    private Long companyId;
    @Schema(description = "拉黑原因")
    private String reason;
}
