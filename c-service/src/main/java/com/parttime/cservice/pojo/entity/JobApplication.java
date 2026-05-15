package com.parttime.cservice.pojo.entity;

import java.time.LocalDateTime;
import java.util.List;

public class JobApplication {
    private Long id;
    private Long workerId;
    private Long jobId;
    private Long companyId;
    private List<Long> scheduleIds;
    private String status;
    private LocalDateTime appliedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public JobApplication() {}

    public JobApplication(Long id, Long workerId, Long jobId, List<Long> scheduleIds,
                          String status, LocalDateTime appliedAt) {
        this.id = id;
        this.workerId = workerId;
        this.jobId = jobId;
        this.scheduleIds = scheduleIds;
        this.status = status;
        this.appliedAt = appliedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getWorkerId() { return workerId; }
    public void setWorkerId(Long workerId) { this.workerId = workerId; }
    public Long getJobId() { return jobId; }
    public void setJobId(Long jobId) { this.jobId = jobId; }
    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }
    public List<Long> getScheduleIds() { return scheduleIds; }
    public void setScheduleIds(List<Long> scheduleIds) { this.scheduleIds = scheduleIds; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getAppliedAt() { return appliedAt; }
    public void setAppliedAt(LocalDateTime appliedAt) { this.appliedAt = appliedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
