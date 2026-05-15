package com.parttime.cservice.core.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class WithdrawalRecord {

    private Long id;
    private Long workerId;
    private BigDecimal amount;
    private String status;
    private LocalDateTime requestedAt;
    private LocalDateTime completedAt;

    public WithdrawalRecord() {}

    public WithdrawalRecord(Long id, Long workerId, BigDecimal amount, String status,
                            LocalDateTime requestedAt, LocalDateTime completedAt) {
        this.id = id;
        this.workerId = workerId;
        this.amount = amount;
        this.status = status;
        this.requestedAt = requestedAt;
        this.completedAt = completedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getWorkerId() { return workerId; }
    public void setWorkerId(Long workerId) { this.workerId = workerId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getRequestedAt() { return requestedAt; }
    public void setRequestedAt(LocalDateTime requestedAt) { this.requestedAt = requestedAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
}
