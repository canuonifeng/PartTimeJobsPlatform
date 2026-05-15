package com.parttime.cservice.core.model;

import com.parttime.cservice.core.dto.JobRateInfo;
import com.parttime.cservice.core.dto.JobScheduleInfo;

import java.time.LocalDateTime;
import java.util.List;

public class Job {
    private Long id;
    private String title;
    private String description;
    private String location;
    private Long categoryId;
    private String categoryName;
    private List<JobRateInfo> rates;
    private List<JobScheduleInfo> schedules;
    private Integer headcount;
    private Integer acceptedCount;
    private LocalDateTime deadline;
    private String status;

    public Job() {}

    public Job(Long id, String title, String description, String location, Long categoryId,
               String categoryName, List<JobRateInfo> rates, List<JobScheduleInfo> schedules,
               Integer headcount, Integer acceptedCount, LocalDateTime deadline, String status) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.location = location;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.rates = rates;
        this.schedules = schedules;
        this.headcount = headcount;
        this.acceptedCount = acceptedCount;
        this.deadline = deadline;
        this.status = status;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public List<JobRateInfo> getRates() { return rates; }
    public void setRates(List<JobRateInfo> rates) { this.rates = rates; }
    public List<JobScheduleInfo> getSchedules() { return schedules; }
    public void setSchedules(List<JobScheduleInfo> schedules) { this.schedules = schedules; }
    public Integer getHeadcount() { return headcount; }
    public void setHeadcount(Integer headcount) { this.headcount = headcount; }
    public Integer getAcceptedCount() { return acceptedCount; }
    public void setAcceptedCount(Integer acceptedCount) { this.acceptedCount = acceptedCount; }
    public LocalDateTime getDeadline() { return deadline; }
    public void setDeadline(LocalDateTime deadline) { this.deadline = deadline; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
