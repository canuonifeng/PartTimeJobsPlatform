package com.parttime.enterprise.pojo.cmd;

import lombok.Data;
import java.util.List;

@Data
public class ScheduleTemplateCmd {

    private Long companyId;
    private String name;
    private String description;
    private List<ScheduleTemplateSlotCmd> slots;
}
