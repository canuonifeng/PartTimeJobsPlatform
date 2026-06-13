package com.parttime.enterprise.pojo.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class OperationExceptionVO {
    private String id;
    private String type;
    private String title;
    private String description;
    private String status;
    private String priority;
    private Long bizId;
    private String routePath;
    private LocalDateTime createdAt;
}
