package com.parttime.enterprise.pojo.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class EvaluationVO {

    private Long id;
    private Long companyId;
    private Long jobId;
    private Long workerId;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
}
