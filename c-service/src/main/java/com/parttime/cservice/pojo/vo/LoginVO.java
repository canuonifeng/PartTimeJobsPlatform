package com.parttime.cservice.pojo.vo;

import lombok.Data;

@Data
public class LoginVO {

    private String token;
    private Long workerId;
    private String openId;
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
