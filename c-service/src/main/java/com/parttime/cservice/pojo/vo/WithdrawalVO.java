package com.parttime.cservice.pojo.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class WithdrawalVO {

    private Long id;
    private Long workerId;
    private BigDecimal amount;
    private String status;
    private LocalDateTime requestedAt;
}
