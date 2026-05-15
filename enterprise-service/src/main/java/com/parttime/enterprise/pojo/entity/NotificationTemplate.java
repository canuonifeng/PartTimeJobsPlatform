package com.parttime.enterprise.pojo.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NotificationTemplate {

    private Long id;
    private String type;
    private String channel;
    private String titleTemplate;
    private String contentTemplate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
