package com.parttime.enterprise.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ScheduleExportVO {
    @Schema(description = "文件名")
    private String filename;
    @Schema(description = "CSV内容")
    private String content;
}
