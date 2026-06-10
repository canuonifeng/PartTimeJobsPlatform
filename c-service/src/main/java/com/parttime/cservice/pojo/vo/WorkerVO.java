package com.parttime.cservice.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class WorkerVO {
    @Schema(description = "工人ID")
    private Long id;
    @Schema(description = "姓名")
    private String name;
    @Schema(description = "手机号")
    private String phone;
    @Schema(description = "微信OpenID")
    private String openId;
    @Schema(description = "头像URL")
    private String avatarUrl;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
