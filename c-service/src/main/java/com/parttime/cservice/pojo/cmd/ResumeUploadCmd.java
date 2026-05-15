package com.parttime.cservice.pojo.cmd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ResumeUploadCmd {

    @Schema(description = "文件名")
    private String fileName;
    @Schema(description = "文件URL")
    private String fileUrl;
}
