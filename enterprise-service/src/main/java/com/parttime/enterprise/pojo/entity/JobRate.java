package com.parttime.enterprise.pojo.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class JobRate {

    private Long id;
    private Long jobId;
    private String type;
    private BigDecimal amount;
    private String currency;
    private String rules;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
