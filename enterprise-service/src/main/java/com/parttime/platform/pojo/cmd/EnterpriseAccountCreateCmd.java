package com.parttime.platform.pojo.cmd;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class EnterpriseAccountCreateCmd {
    @Schema(description = "企业ID")
    private Long enterpriseId;
    @Schema(description = "登录名")
    private String username;
    @Schema(description = "密码")
    private String password;
    @Schema(description = "显示名")
    private String displayName;
    @Schema(description = "角色: ADMIN/HR/MANAGER/FINANCE")
    private String role;
}
