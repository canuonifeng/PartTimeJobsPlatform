package com.parttime.platform.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "OSS 签名 URL")
public class SignedUrlVO {

    @Schema(description = "签名后的访问 URL")
    private String url;

    @Schema(description = "过期时间（毫秒时间戳）")
    private long expiresAt;
}
