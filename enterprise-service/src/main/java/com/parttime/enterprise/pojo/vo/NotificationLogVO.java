package com.parttime.enterprise.pojo.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NotificationLogVO {

    private Long id;
    private Long recipientId;
    private String recipientType;
    private String type;
    private String channel;
    private String title;
    private String content;
    private String status;
    private String errorMessage;
    private LocalDateTime sentAt;
    private LocalDateTime createdAt;
}
