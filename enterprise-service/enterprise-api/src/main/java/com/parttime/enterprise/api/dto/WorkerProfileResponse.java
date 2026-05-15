package com.parttime.enterprise.api.dto;

import java.util.List;

public class WorkerProfileResponse {

    private Long workerId;
    private Double avgRating;
    private Integer totalEvaluations;
    private Boolean isBlacklisted;
    private String blacklistReason;
    private List<WorkHistoryResponse> workHistory;

    public Long getWorkerId() { return workerId; }
    public void setWorkerId(Long workerId) { this.workerId = workerId; }

    public Double getAvgRating() { return avgRating; }
    public void setAvgRating(Double avgRating) { this.avgRating = avgRating; }

    public Integer getTotalEvaluations() { return totalEvaluations; }
    public void setTotalEvaluations(Integer totalEvaluations) { this.totalEvaluations = totalEvaluations; }

    public Boolean getIsBlacklisted() { return isBlacklisted; }
    public void setIsBlacklisted(Boolean isBlacklisted) { this.isBlacklisted = isBlacklisted; }

    public String getBlacklistReason() { return blacklistReason; }
    public void setBlacklistReason(String blacklistReason) { this.blacklistReason = blacklistReason; }

    public List<WorkHistoryResponse> getWorkHistory() { return workHistory; }
    public void setWorkHistory(List<WorkHistoryResponse> workHistory) { this.workHistory = workHistory; }
}
