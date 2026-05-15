package com.parttime.cservice.pojo.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ApplicationVO {
    private Long applicationId;
    private Long jobId;
    private String status;
    private LocalDateTime appliedAt;
}
