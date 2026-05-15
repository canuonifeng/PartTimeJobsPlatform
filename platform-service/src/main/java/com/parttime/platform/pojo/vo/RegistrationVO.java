package com.parttime.platform.pojo.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RegistrationVO {

    private Long id;
    private String companyName;
    private String contactName;
    private String contactPhone;
    private String companyAddress;
    private String businessLicense;
    private String status;
    private String reviewerId;
    private String reviewRemark;
    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
