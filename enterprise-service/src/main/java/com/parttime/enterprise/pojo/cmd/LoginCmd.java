package com.parttime.enterprise.pojo.cmd;

public record LoginCmd(
        @io.swagger.v3.oas.annotations.media.Schema(description = "用户名") String username,
        @io.swagger.v3.oas.annotations.media.Schema(description = "密码") String password) {
}
