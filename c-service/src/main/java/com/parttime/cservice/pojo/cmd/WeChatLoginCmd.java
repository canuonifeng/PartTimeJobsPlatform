package com.parttime.cservice.pojo.cmd;

import io.swagger.v3.oas.annotations.media.Schema;

public record WeChatLoginCmd(
        @Schema(description = "微信登录code") String code,
        @Schema(description = "邀请码") String referralCode) {
}
