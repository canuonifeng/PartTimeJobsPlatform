package com.parttime.enterprise.pojo.cmd;

import java.util.List;

public class ScheduleTemplateCmd {

    private Long companyId;
    private String name;
    private String description;
    private List<ScheduleTemplateSlotCmd> slots;

    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<ScheduleTemplateSlotCmd> getSlots() { return slots; }
    public void setSlots(List<ScheduleTemplateSlotCmd> slots) { this.slots = slots; }
}
