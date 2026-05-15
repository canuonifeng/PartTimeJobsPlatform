package com.parttime.enterprise.api.dto;

import java.util.List;

public class ScheduleTemplateRequest {

    private Long companyId;
    private String name;
    private String description;
    private List<ScheduleTemplateSlotRequest> slots;

    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<ScheduleTemplateSlotRequest> getSlots() { return slots; }
    public void setSlots(List<ScheduleTemplateSlotRequest> slots) { this.slots = slots; }
}
