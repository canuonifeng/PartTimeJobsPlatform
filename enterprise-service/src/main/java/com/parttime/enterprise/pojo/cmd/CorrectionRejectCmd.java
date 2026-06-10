package com.parttime.enterprise.pojo.cmd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class CorrectionRejectCmd {

    @Schema(description = "拒绝原因", requiredMode = Schema.RequiredMode.REQUIRED)
    private String rejectReason;
}
