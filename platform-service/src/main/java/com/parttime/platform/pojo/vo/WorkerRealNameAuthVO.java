package com.parttime.platform.pojo.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class WorkerRealNameAuthVO {
    private Long id;
    private Long workerId;
    private String workerName;
    private String workerPhone;
    private String realName;
    private String idCardNo;
    private String idCardFrontUrl;
    private String idCardBackUrl;
    private String status;
    private String rejectReason;
    private LocalDateTime submittedAt;
    private LocalDateTime reviewedAt;
    private Long reviewerId;
}
