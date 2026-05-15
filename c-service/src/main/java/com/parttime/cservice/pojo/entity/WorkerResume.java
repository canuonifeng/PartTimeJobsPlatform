package com.parttime.cservice.pojo.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class WorkerResume {

    private Long id;
    private Long workerId;
    private String fileName;
    private String fileUrl;
    private LocalDateTime uploadedAt;

    public WorkerResume() {}
}
