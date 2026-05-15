package com.parttime.cservice.pojo.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NotificationVO {

    private Long id;
    private Long recipientId;
    private String recipientType;
    private String type;
    private String title;
    private String content;
    private String status;
    private LocalDateTime sentAt;
}
