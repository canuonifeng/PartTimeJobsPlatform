package com.parttime.cservice.pojo.cmd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class CorrectionSubmitCmd {
    @Schema(description = "班次ID")
    private Long shiftId;
    @Schema(description = "补卡原因")
    private String reason;
}
