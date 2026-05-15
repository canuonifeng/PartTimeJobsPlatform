package com.parttime.platform.pojo.cmd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ReviewJobReportCmd {

    @Schema(description = "审核备注")
    private String remark;
}
