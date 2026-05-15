package com.parttime.cservice.pojo.entity;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class JobApplication {
    private Long id;
    private Long workerId;
    private Long jobId;
    private Long companyId;
    private List<Long> scheduleIds;
    private String status;
    private LocalDateTime appliedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public JobApplication() {}
}
