package com.parttime.cservice.pojo.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Worker {
    @Schema(description = "工人ID")
    private Long id;
    @Schema(description = "姓名")
    private String name;
    @Schema(description = "手机号")
    private String phone;
    @Schema(description = "微信授权码")
    private String wechatCode;
    @Schema(description = "微信OpenID")
    private String openId;
    @Schema(description = "头像URL")
    private String avatarUrl;
    @Schema(description = "状态: ACTIVE-正常, DISABLED-禁用")
    private String status;
    @Schema(description = "昵称")
    private String nickname;
    @Schema(description = "头像")
    private String avatar;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    public Worker() {}
}
