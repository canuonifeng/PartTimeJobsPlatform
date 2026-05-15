package com.parttime.enterprise.api.dto;

import java.math.BigDecimal;

public class JobRateRequest {

    private JobRateType type;
    private BigDecimal amount;
    private String currency;
    private String rules;

    public JobRateType getType() { return type; }
    public void setType(JobRateType type) { this.type = type; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getRules() { return rules; }
    public void setRules(String rules) { this.rules = rules; }
}
