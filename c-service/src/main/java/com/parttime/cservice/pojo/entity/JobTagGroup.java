package com.parttime.cservice.pojo.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class JobTagGroup {

    private Long id;
    private String name;
    private String code;
    private Integer sortOrder;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
