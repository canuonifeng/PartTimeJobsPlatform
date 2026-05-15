package com.parttime.cservice.core.dto;

import java.math.BigDecimal;

public class JobRateInfo {
    private Long id;
    private String type;
    private BigDecimal amount;
    private String currency;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
}
