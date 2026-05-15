package com.parttime.cservice.pojo.vo;

import java.math.BigDecimal;
import java.util.List;

public class JobSummaryVO {
    private Long id;
    private String title;
    private String location;
    private String categoryName;
    private BigDecimal minRate;
    private BigDecimal maxRate;
    private List<String> rateTypes;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public BigDecimal getMinRate() { return minRate; }
    public void setMinRate(BigDecimal minRate) { this.minRate = minRate; }
    public BigDecimal getMaxRate() { return maxRate; }
    public void setMaxRate(BigDecimal maxRate) { this.maxRate = maxRate; }
    public List<String> getRateTypes() { return rateTypes; }
    public void setRateTypes(List<String> rateTypes) { this.rateTypes = rateTypes; }
}
