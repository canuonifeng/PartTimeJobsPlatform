package com.parttime.enterprise.pojo.cmd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;

@Data
public class ScheduleTemplateCmd {

    @Schema(description = "企业ID")
    private Long companyId;
    @Schema(description = "模板名称")
    private String name;
    @Schema(description = "模板描述")
    private String description;
    @Schema(description = "时段列表")
    private List<ScheduleTemplateSlotCmd> slots;
}
