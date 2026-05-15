package com.parttime.enterprise.pojo.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class WorkerEvaluation {

    private Long id;
    private Long companyId;
    private Long jobId;
    private Long workerId;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
}
