package com.parttime.platform.pojo.cmd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class WithdrawalQueryCmd {
    @Schema(description = "状态")
    private String status;
    @Schema(description = "关键词")
    private String keyword;
}
