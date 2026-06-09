package com.parttime.cservice.pojo.cmd;

import io.swagger.v3.oas.annotations.media.Schema;

public record WeChatPhoneLoginCmd(
        @Schema(description = "微信登录code") String code,
        @Schema(description = "手机号加密数据") String encryptedData,
        @Schema(description = "加密初始向量") String iv
) {
}
