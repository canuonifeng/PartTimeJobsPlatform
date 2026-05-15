package com.parttime.cservice.pojo.cmd;

public record WeChatLoginCmd(@io.swagger.v3.oas.annotations.media.Schema(description = "微信登录code") String code) {
}
