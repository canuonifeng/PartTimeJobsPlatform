package com.parttime.platform.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "运营账号VO")
public class OperatorVO {

    @Schema(description = "账号ID")
    private Long id;
    @Schema(description = "用户名")
    private String username;
    @Schema(description = "姓名")
    private String realName;
    @Schema(description = "手机号")
    private String phone;
    @Schema(description = "邮箱")
    private String email;
    @Schema(description = "角色")
    private String role;
    @Schema(description = "角色名称")
    private String roleName;
    @Schema(description = "权限列表")
    private List<String> permissions;
    @Schema(description = "状态")
    private String status;
    @Schema(description = "最后登录时间")
    private LocalDateTime lastLoginAt;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
