package com.parttime.cservice.pojo.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class JobDetailVO {
    private Long id;
    private String title;
    private String description;
    private String location;
    private String categoryName;
    private List<JobRateInfoVO> rates;
    private List<JobScheduleInfoVO> schedules;
    private Integer headcount;
    private Integer acceptedCount;
    private LocalDateTime deadline;
    private String status;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public List<JobRateInfoVO> getRates() { return rates; }
    public void setRates(List<JobRateInfoVO> rates) { this.rates = rates; }
    public List<JobScheduleInfoVO> getSchedules() { return schedules; }
    public void setSchedules(List<JobScheduleInfoVO> schedules) { this.schedules = schedules; }
    public Integer getHeadcount() { return headcount; }
    public void setHeadcount(Integer headcount) { this.headcount = headcount; }
    public Integer getAcceptedCount() { return acceptedCount; }
    public void setAcceptedCount(Integer acceptedCount) { this.acceptedCount = acceptedCount; }
    public LocalDateTime getDeadline() { return deadline; }
    public void setDeadline(LocalDateTime deadline) { this.deadline = deadline; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
