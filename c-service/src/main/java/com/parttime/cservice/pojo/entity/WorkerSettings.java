package com.parttime.cservice.pojo.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class WorkerSettings {
    private Long id;
    private Long workerId;
    private Boolean pushEnabled;
    private Boolean locationEnabled;
    private Boolean quietEnabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
