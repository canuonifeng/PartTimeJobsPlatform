package com.parttime.enterprise.pojo.cmd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class AccountSecurityUpdateCmd {
    @Schema(description = "姓名")
    private String displayName;
    @Schema(description = "联系电话")
    private String phone;
}
