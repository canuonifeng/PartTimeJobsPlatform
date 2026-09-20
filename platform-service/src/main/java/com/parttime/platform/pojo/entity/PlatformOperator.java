package com.parttime.platform.pojo.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PlatformOperator {

    @Schema(description = "运营账号ID")
    private Long id;
    @Schema(description = "用户名")
    private String username;
    @Schema(description = "密码(加密)")
    private String password;
    @Schema(description = "真实姓名")
    private String realName;
    @Schema(description = "手机号")
    private String phone;
    @Schema(description = "邮箱")
    private String email;
    @Schema(description = "头像URL")
    private String avatar;
    @Schema(description = "角色")
    private String role;
    @Schema(description = "权限列表(JSON)")
    private String permissions;
    @Schema(description = "状态: ACTIVE, INACTIVE, LOCKED")
    private String status;
    @Schema(description = "最后登录IP")
    private String lastLoginIp;
    @Schema(description = "最后登录时间")
    private LocalDateTime lastLoginAt;
    @Schema(description = "登录失败次数")
    private Integer loginFailCount;
    @Schema(description = "锁定时间")
    private LocalDateTime lockTime;
    @Schema(description = "创建人ID")
    private Long operatorId;
    @Schema(description = "创建人姓名")
    private String operatorName;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
