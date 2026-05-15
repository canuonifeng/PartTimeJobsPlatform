package com.parttime.enterprise.pojo.vo;

import lombok.Data;
import com.parttime.enterprise.enums.JobRateType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class JobRateVO {

    private Long id;
    private Long jobId;
    private JobRateType type;
    private BigDecimal amount;
    private String currency;
    private String rules;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
