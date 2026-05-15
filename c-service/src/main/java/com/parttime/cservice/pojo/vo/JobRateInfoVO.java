package com.parttime.cservice.pojo.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class JobRateInfoVO {
    private Long id;
    private String type;
    private BigDecimal amount;
    private String currency;
}
