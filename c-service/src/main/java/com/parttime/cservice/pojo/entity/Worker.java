package com.parttime.cservice.pojo.entity;

import java.time.LocalDateTime;

public class Worker {
    private Long id;
    private String name;
    private String phone;
    private String wechatCode;
    private String openId;
    private String avatarUrl;
    private String status;
    private String nickname;
    private String avatar;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Worker() {}

    public Worker(Long id, String name, String phone, String avatar, String wechatCode, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.avatar = avatar;
        this.wechatCode = wechatCode;
        this.createdAt = createdAt;
    }

    public Worker(Long id, String name, String phone, String avatar, String wechatCode, String nickname, String avatarUrl, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.avatar = avatar;
        this.wechatCode = wechatCode;
        this.nickname = nickname;
        this.avatarUrl = avatarUrl;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getWechatCode() { return wechatCode; }
    public void setWechatCode(String wechatCode) { this.wechatCode = wechatCode; }
    public String getOpenId() { return openId; }
    public void setOpenId(String openId) { this.openId = openId; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getWechatOpenId() { return openId; }
    public void setWechatOpenId(String wechatOpenId) { this.openId = wechatOpenId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
