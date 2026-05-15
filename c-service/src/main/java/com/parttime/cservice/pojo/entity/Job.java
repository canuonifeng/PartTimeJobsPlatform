package com.parttime.cservice.pojo.entity;

import com.parttime.cservice.pojo.vo.JobRateInfoVO;
import com.parttime.cservice.pojo.vo.JobScheduleInfoVO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class Job {
    private Long id;
    private Long jobId;
    private Long companyId;
    private String companyName;
    private String title;
    private String description;
    private String location;
    private Long categoryId;
    private String categoryName;
    private String rateType;
    private BigDecimal rateAmount;
    private String status;
    private LocalDateTime publishedAt;
    private List<JobRateInfoVO> rates;
    private List<JobScheduleInfoVO> schedules;
    private Integer headcount;
    private Integer acceptedCount;
    private LocalDateTime deadline;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Job() {}

    public Job(Long id, String title, String description, String location, Long categoryId,
               String categoryName, List<JobRateInfoVO> rates, List<JobScheduleInfoVO> schedules,
               Integer headcount, Integer acceptedCount, LocalDateTime deadline, String status) {
        this.id = id;
        this.jobId = id;
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
    public Long getJobId() { return jobId; }
    public void setJobId(Long jobId) { this.jobId = jobId; }
    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
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
    public String getRateType() { return rateType; }
    public void setRateType(String rateType) { this.rateType = rateType; }
    public BigDecimal getRateAmount() { return rateAmount; }
    public void setRateAmount(BigDecimal rateAmount) { this.rateAmount = rateAmount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getPublishedAt() { return publishedAt; }
    public void setPublishedAt(LocalDateTime publishedAt) { this.publishedAt = publishedAt; }
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
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
