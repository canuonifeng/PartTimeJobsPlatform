package com.parttime.enterprise.pojo.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ScheduleTemplate {

    @Schema(description = "模板ID")
    private Long id;
    @Schema(description = "企业ID")
    private Long companyId;
    @Schema(description = "模板名称")
    private String name;
    @Schema(description = "模板描述")
    private String description;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
