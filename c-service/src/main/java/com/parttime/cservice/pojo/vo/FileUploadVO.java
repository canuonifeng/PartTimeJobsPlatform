package com.parttime.cservice.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "文件上传结果")
public class FileUploadVO {
    @Schema(description = "文件访问 URL")
    private String url;
    @Schema(description = "文件名")
    private String fileName;
    @Schema(description = "文件大小，单位字节")
    private Long size;
    @Schema(description = "文件类型")
    private String contentType;
}
