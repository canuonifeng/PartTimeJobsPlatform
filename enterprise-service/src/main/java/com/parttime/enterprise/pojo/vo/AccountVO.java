package com.parttime.enterprise.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AccountVO {
    @Schema(description = "账号ID")
    private Long id;
    @Schema(description = "用户名")
    private String username;
    @Schema(description = "显示名")
    private String displayName;
    @Schema(description = "角色")
    private String role;
    @Schema(description = "状态")
    private String status;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
