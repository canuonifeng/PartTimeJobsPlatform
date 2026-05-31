package com.parttime.platform.pojo.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EnterpriseRealNameAuthVO {
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
}
