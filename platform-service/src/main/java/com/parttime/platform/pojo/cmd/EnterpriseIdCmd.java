package com.parttime.platform.pojo.cmd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class EnterpriseIdCmd {
    @Schema(description = "企业ID")
    private Long enterpriseId;
}
