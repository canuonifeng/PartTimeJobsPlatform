package com.parttime.enterprise.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "OSS 直传 STS 临时凭证")
public class OssStsVO {

    @Schema(description = "OSS Bucket 名称")
    private String bucket;

    @Schema(description = "OSS Endpoint")
    private String endpoint;

    @Schema(description = "临时 AccessKeyId")
    private String accessKeyId;

    @Schema(description = "临时 AccessKeySecret")
    private String accessKeySecret;

    @Schema(description = "临时 SecurityToken")
    private String securityToken;

    @Schema(description = "过期时间（ISO 字符串）")
    private String expiration;

    @Schema(description = "本次允许上传的对象前缀")
    private String prefix;
}
