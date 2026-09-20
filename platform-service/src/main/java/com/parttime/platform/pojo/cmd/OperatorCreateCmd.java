package com.parttime.platform.pojo.cmd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "运营账号创建命令")
public class OperatorCreateCmd {

    @Schema(description = "用户名")
    private String username;
    @Schema(description = "初始密码")
    private String password;
    @Schema(description = "真实姓名")
    private String realName;
    @Schema(description = "手机号")
    private String phone;
    @Schema(description = "邮箱")
    private String email;
    @Schema(description = "角色")
    private String role;
}
