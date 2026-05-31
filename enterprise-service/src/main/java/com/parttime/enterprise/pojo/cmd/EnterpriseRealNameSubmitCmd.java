package com.parttime.enterprise.pojo.cmd;

import lombok.Data;

@Data
public class EnterpriseRealNameSubmitCmd {
    private String legalPersonName;
    private String legalPersonIdCard;
    private String unifiedSocialCreditCode;
    private String businessLicenseUrl;
}
