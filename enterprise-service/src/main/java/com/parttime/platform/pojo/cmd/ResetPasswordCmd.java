package com.parttime.platform.pojo.cmd;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ResetPasswordCmd {
    @Schema(description = "账号ID")
    private Long id;
    @Schema(description = "新密码")
    private String newPassword;
}
