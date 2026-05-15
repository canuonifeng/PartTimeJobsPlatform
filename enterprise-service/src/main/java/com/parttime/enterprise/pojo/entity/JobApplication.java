package com.parttime.enterprise.pojo.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class JobApplication {

    private Long id;
    private Long jobId;
    private Long workerId;
    private String status;
    private LocalDateTime appliedAt;
    private LocalDateTime updatedAt;
}
