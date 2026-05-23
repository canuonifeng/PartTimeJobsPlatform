package com.parttime.platform.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class WorkerVO {
    @Schema(description = "兼职ID")
    private Long id;
    @Schema(description = "姓名")
    private String name;
    @Schema(description = "电话")
    private String phone;
    @Schema(description = "微信标识")
    private String wechatCode;
    @Schema(description = "OpenID")
    private String openId;
    @Schema(description = "头像URL")
    private String avatarUrl;
    @Schema(description = "技能标签")
    private String skills;
    @Schema(description = "可用时间")
    private String availableDays;
    @Schema(description = "状态: ACTIVE/DISABLED")
    private String status;
    @Schema(description = "注册时间")
    private LocalDateTime createdAt;
}
