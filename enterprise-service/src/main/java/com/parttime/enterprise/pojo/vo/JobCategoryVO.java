package com.parttime.enterprise.pojo.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class JobCategoryVO {

    private Long id;
    private String name;
    private Long parentId;
    private Integer sortOrder;
    private List<JobCategoryVO> children;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
