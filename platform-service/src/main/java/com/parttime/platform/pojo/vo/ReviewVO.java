package com.parttime.platform.pojo.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReviewVO {
    private Long id;
    private String reviewType;
    private Long reviewerId;
    private String reviewerName;
    private String reviewerType;
    private Long revieweeId;
    private String revieweeName;
    private String revieweeType;
    private Long jobId;
    private String jobTitle;
    private Long scheduleId;
    private Integer rating;
    private String content;
    private String images;
    private String tags;
    private Boolean isAnonymous;
    private Boolean isViolation;
    private String violationReason;
    private LocalDateTime violationHandledAt;
    private String violationHandlerName;
    private String replyContent;
    private LocalDateTime replyAt;
    private String status;
    private Integer helpfulCount;
    private Integer reportCount;
    private LocalDateTime createdAt;
}
