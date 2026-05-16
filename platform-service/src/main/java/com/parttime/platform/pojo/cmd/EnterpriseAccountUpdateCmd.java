package com.parttime.platform.pojo.cmd;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class EnterpriseAccountUpdateCmd {
    @Schema(description = "账号ID")
    private Long id;
    @Schema(description = "显示名")
    private String displayName;
    @Schema(description = "角色")
    private String role;
}
