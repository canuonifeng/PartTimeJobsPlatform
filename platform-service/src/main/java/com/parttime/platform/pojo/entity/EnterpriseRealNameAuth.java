package com.parttime.platform.pojo.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EnterpriseRealNameAuth {
    private Long id;
    private Long enterpriseId;
    private String companyName;
    private String legalPersonName;
    private String legalPersonIdCard;
    private String unifiedSocialCreditCode;
    private String businessLicenseUrl;
    private String status;
    private String rejectReason;
    private LocalDateTime submittedAt;
    private LocalDateTime reviewedAt;
    private Long reviewerId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
