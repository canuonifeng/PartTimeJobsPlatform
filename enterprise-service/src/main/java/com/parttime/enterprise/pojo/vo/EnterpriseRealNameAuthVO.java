package com.parttime.enterprise.pojo.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EnterpriseRealNameAuthVO {
    private String status;
    private String legalPersonName;
    private String legalPersonIdCardMasked;
    private String unifiedSocialCreditCode;
    private String businessLicenseUrl;
    private String rejectReason;
    private LocalDateTime submittedAt;
    private LocalDateTime reviewedAt;
}
