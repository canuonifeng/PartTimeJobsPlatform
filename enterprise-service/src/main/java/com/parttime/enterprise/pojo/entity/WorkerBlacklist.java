package com.parttime.enterprise.pojo.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class WorkerBlacklist {

    private Long id;
    private Long companyId;
    private Long workerId;
    private String reason;
    private LocalDateTime createdAt;
}
