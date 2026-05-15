package com.parttime.enterprise.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ScheduleTemplateVO {

    @Schema(description = "模板ID")
    private Long id;
    @Schema(description = "企业ID")
    private Long companyId;
    @Schema(description = "模板名称")
    private String name;
    @Schema(description = "模板描述")
    private String description;
    @Schema(description = "时段列表")
    private List<ScheduleTemplateSlotVO> slots;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
