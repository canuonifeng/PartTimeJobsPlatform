package com.parttime.platform.pojo.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReferralRecord {

    @Schema(description = "记录ID")
    private Long id;
    @Schema(description = "推荐人ID")
    private Long referrerId;
    @Schema(description = "被推荐人ID")
    private Long refereeId;
    @Schema(description = "推荐码")
    private String referralCode;
    @Schema(description = "绑定时间")
    private LocalDateTime boundAt;
}