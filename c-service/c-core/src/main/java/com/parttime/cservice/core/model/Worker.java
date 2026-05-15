package com.parttime.cservice.core.model;

import java.time.LocalDateTime;

public class Worker {
    private Long id;
    private String name;
    private String phone;
    private String avatar;
    private String wechatOpenId;
    private String nickname;
    private String avatarUrl;
    private LocalDateTime createdAt;

    public Worker() {}

    public Worker(Long id, String name, String phone, String avatar, String wechatOpenId, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.avatar = avatar;
        this.wechatOpenId = wechatOpenId;
        this.createdAt = createdAt;
    }

    public Worker(Long id, String name, String phone, String avatar, String wechatOpenId, String nickname, String avatarUrl, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.avatar = avatar;
        this.wechatOpenId = wechatOpenId;
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
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public String getWechatOpenId() { return wechatOpenId; }
    public void setWechatOpenId(String wechatOpenId) { this.wechatOpenId = wechatOpenId; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
