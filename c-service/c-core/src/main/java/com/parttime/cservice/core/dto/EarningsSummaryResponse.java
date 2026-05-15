package com.parttime.cservice.core.dto;

import java.math.BigDecimal;

public class EarningsSummaryResponse {

    private BigDecimal totalEarned;
    private BigDecimal totalWithdrawn;
    private BigDecimal pendingWithdrawal;

    public BigDecimal getTotalEarned() { return totalEarned; }
    public void setTotalEarned(BigDecimal totalEarned) { this.totalEarned = totalEarned; }

    public BigDecimal getTotalWithdrawn() { return totalWithdrawn; }
    public void setTotalWithdrawn(BigDecimal totalWithdrawn) { this.totalWithdrawn = totalWithdrawn; }

    public BigDecimal getPendingWithdrawal() { return pendingWithdrawal; }
    public void setPendingWithdrawal(BigDecimal pendingWithdrawal) { this.pendingWithdrawal = pendingWithdrawal; }
}
