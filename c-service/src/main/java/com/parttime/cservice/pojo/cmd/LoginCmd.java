package com.parttime.cservice.pojo.cmd;

public record LoginCmd(@io.swagger.v3.oas.annotations.media.Schema(description = "微信授权码") String wechatCode) {
}
