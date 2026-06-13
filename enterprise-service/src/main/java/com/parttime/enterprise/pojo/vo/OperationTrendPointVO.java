package com.parttime.enterprise.pojo.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class OperationTrendPointVO {
    private LocalDate date;
    private Long applicationCount;
    private Long shiftCount;
    private BigDecimal salaryAmount;
}
