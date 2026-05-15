package com.parttime.cservice.pojo.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class JobSummaryVO {
    private Long id;
    private String title;
    private String location;
    private String categoryName;
    private BigDecimal minRate;
    private BigDecimal maxRate;
    private List<String> rateTypes;
}
