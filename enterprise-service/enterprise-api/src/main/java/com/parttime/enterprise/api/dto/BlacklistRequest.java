package com.parttime.enterprise.api.dto;

public class BlacklistRequest {

    private Long companyId;
    private String reason;

    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
