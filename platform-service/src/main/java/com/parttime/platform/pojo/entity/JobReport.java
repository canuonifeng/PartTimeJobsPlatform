package com.parttime.platform.pojo.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class JobReport {

    private Long id;
    private Long jobId;
    private Long reporterId;
    private String reason;
    private String description;
    private String status;
    private String reviewerId;
    private String reviewRemark;
    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
