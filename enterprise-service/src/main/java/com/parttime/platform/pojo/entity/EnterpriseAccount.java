package com.parttime.platform.pojo.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class EnterpriseAccount {
    @Schema(description = "账号ID")
    private Long id;
    @Schema(description = "企业ID")
    private Long enterpriseId;
    @Schema(description = "登录名")
    private String username;
    @Schema(description = "密码(BCrypt)")
    private String password;
    @Schema(description = "显示名")
    private String displayName;
    @Schema(description = "角色: ADMIN/HR/MANAGER/FINANCE")
    private String role;
    @Schema(description = "状态: ACTIVE/DISABLED")
    private String status;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
