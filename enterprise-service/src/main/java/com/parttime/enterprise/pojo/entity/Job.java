package com.parttime.enterprise.pojo.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Job {

    private Long id;
    private Long companyId;
    private String title;
    private String description;
    private String location;
    private Long categoryId;
    private Integer headcount;
    private String status;
    private LocalDateTime deadline;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
