package com.parttime.cservice.pojo.cmd;

public record RegisterCmd(
        @io.swagger.v3.oas.annotations.media.Schema(description = "姓名") String name,
        @io.swagger.v3.oas.annotations.media.Schema(description = "手机号") String phone,
        @io.swagger.v3.oas.annotations.media.Schema(description = "头像") String avatar,
        @io.swagger.v3.oas.annotations.media.Schema(description = "邀请码") String referralCode) {
}
