package com.parttime.cservice.core.dto;

public class WeChatLoginResponse {

    private String token;
    private Long workerId;
    private String openId;
    private String nickname;

    public WeChatLoginResponse() {}

    public WeChatLoginResponse(String token, Long workerId, String openId, String nickname) {
        this.token = token;
        this.workerId = workerId;
        this.openId = openId;
        this.nickname = nickname;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public Long getWorkerId() { return workerId; }
    public void setWorkerId(Long workerId) { this.workerId = workerId; }
    public String getOpenId() { return openId; }
    public void setOpenId(String openId) { this.openId = openId; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
}
