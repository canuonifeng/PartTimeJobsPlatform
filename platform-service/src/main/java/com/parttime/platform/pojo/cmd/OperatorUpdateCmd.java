package com.parttime.platform.pojo.cmd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "运营账号更新命令")
public class OperatorUpdateCmd {

    @Schema(description = "账号ID")
    private Long id;
    @Schema(description = "真实姓名")
    private String realName;
    @Schema(description = "手机号")
    private String phone;
    @Schema(description = "邮箱")
    private String email;
    @Schema(description = "角色")
    private String role;
    @Schema(description = "自定义权限列表")
    private List<String> permissions;
}
