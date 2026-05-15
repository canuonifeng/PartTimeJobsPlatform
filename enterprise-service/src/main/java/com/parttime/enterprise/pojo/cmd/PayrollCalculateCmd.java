package com.parttime.enterprise.pojo.cmd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class PayrollCalculateCmd {

    @Schema(description = "批次ID")
    private Long batchId;
}
