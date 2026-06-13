package com.parttime.enterprise.pojo.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OperationTodoItemVO {
    private String id;
    private String type;
    private String title;
    private String description;
    private String status;
    private String priority;
    private Long bizId;
    private String bizType;
    private String actionText;
    private String routePath;
    private LocalDateTime createdAt;
    private LocalDateTime deadlineAt;
    private List<String> actions;
}
