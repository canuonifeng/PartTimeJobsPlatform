package com.parttime.cservice.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class LoginVO {

    @Schema(description = "JWT令牌")
    private String token;
    @Schema(description = "工人ID")
    private Long workerId;
    @Schema(description = "微信OpenID")
    private String openId;
    @Schema(description = "微信昵称")
    private String nickname;

    public LoginVO(String token, Long workerId) {
        this.token = token;
        this.workerId = workerId;
    }

    public LoginVO(String token, Long workerId, String openId, String nickname) {
        this.token = token;
        this.workerId = workerId;
        this.openId = openId;
        this.nickname = nickname;
    }
}
