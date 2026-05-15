package com.parttime.enterprise.api.dto;

import java.time.LocalDateTime;
import java.util.List;

public class ScheduleTemplateResponse {

    private Long id;
    private Long companyId;
    private String name;
    private String description;
    private List<ScheduleTemplateSlotResponse> slots;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<ScheduleTemplateSlotResponse> getSlots() { return slots; }
    public void setSlots(List<ScheduleTemplateSlotResponse> slots) { this.slots = slots; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
