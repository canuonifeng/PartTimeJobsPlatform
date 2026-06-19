package com.parttime.cservice.pojo.cmd;

import io.swagger.v3.oas.annotations.media.Schema;

public record PhoneLoginCmd(
        @Schema(description = "手机号") String phone,
        @Schema(description = "短信验证码") String code,
        @Schema(description = "邀请码") String referralCode) {
}
