package com.parttime.enterprise.pojo.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CompanyWorker {
    private Long id;
    private Long companyId;
    private Long workerId;
    private String status;
    private LocalDateTime firstContactAt;
    private LocalDateTime lastContactAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
