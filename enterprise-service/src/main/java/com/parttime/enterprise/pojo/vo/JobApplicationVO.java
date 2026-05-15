package com.parttime.enterprise.pojo.vo;

import lombok.Data;
import com.parttime.enterprise.enums.ApplicationStatus;
import java.time.LocalDateTime;

@Data
public class JobApplicationVO {

    private Long id;
    private Long jobId;
    private Long workerId;
    private ApplicationStatus status;
    private LocalDateTime appliedAt;
    private LocalDateTime updatedAt;
}
