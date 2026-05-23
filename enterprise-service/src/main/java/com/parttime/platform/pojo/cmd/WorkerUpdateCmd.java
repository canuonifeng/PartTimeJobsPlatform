package com.parttime.platform.pojo.cmd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class WorkerUpdateCmd {
    @Schema(description = "兼职ID")
    private Long id;
    @Schema(description = "姓名")
    private String name;
    @Schema(description = "电话")
    private String phone;
}
