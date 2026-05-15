package com.parttime.enterprise.api.dto;

import java.time.LocalDateTime;
import java.util.List;

public class JobResponse {

    private Long id;
    private Long companyId;
    private String title;
    private String description;
    private String location;
    private Long categoryId;
    private Integer headcount;
    private JobStatus status;
    private LocalDateTime deadline;
    private List<JobRateResponse> rates;
    private List<JobScheduleResponse> schedules;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public Integer getHeadcount() { return headcount; }
    public void setHeadcount(Integer headcount) { this.headcount = headcount; }

    public JobStatus getStatus() { return status; }
    public void setStatus(JobStatus status) { this.status = status; }

    public LocalDateTime getDeadline() { return deadline; }
    public void setDeadline(LocalDateTime deadline) { this.deadline = deadline; }

    public List<JobRateResponse> getRates() { return rates; }
    public void setRates(List<JobRateResponse> rates) { this.rates = rates; }

    public List<JobScheduleResponse> getSchedules() { return schedules; }
    public void setSchedules(List<JobScheduleResponse> schedules) { this.schedules = schedules; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
