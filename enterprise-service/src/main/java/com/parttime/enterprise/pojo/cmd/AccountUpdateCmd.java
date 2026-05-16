package com.parttime.enterprise.pojo.cmd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class AccountUpdateCmd {
    @Schema(description = "账号ID")
    private Long id;
    @Schema(description = "显示名")
    private String displayName;
    @Schema(description = "角色: ADMIN/HR/MANAGER/FINANCE")
    private String role;
}
