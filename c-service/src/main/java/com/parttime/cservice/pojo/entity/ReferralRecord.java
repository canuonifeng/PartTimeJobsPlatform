package com.parttime.cservice.pojo.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReferralRecord {
    private Long id;
    private Long referrerId;
    private Long refereeId;
    private String referralCode;
    private LocalDateTime boundAt;
}
