package com.parttime.enterprise.pojo.cmd;

import java.time.LocalDateTime;
import java.util.List;

public class UpdateJobCmd {

    private Long companyId;
    private String title;
    private String description;
    private String location;
    private Long categoryId;
    private Integer headcount;
    private LocalDateTime deadline;
    private List<JobRateCmd> rates;
    private List<JobScheduleCmd> schedules;

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

    public LocalDateTime getDeadline() { return deadline; }
    public void setDeadline(LocalDateTime deadline) { this.deadline = deadline; }

    public List<JobRateCmd> getRates() { return rates; }
    public void setRates(List<JobRateCmd> rates) { this.rates = rates; }

    public List<JobScheduleCmd> getSchedules() { return schedules; }
    public void setSchedules(List<JobScheduleCmd> schedules) { this.schedules = schedules; }
}
