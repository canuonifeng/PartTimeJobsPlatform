package com.parttime.platform.pojo.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class JobCategory {

    private Long id;
    private String name;
    private Long parentId;
    private Integer sortOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
