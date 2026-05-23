package com.parttime.cservice.pojo.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class WorkerResume {

    @Schema(description = "简历ID")
    private Long id;
    @Schema(description = "工人ID")
    private Long workerId;
    @Schema(description = "文件名")
    private String fileName;
    @Schema(description = "文件URL")
    private String fileUrl;
    @Schema(description = "上传时间")
    private LocalDateTime uploadedAt;

    public WorkerResume() {}
}
