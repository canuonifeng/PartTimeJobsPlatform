package com.parttime.cservice.pojo.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class RefereeVO {
    private Long id;
    private String name;
    private String phone;
    private int workCount;
    private BigDecimal workHours;
    private String rewardStatus;  // PENDING, AUDITING, GRANTED, REJECTED, NOT_QUALIFIED
    private String boundAt;
}
