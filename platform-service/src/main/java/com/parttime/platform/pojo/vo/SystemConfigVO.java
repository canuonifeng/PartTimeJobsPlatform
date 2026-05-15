package com.parttime.platform.pojo.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SystemConfigVO {

    private Long id;
    private String configKey;
    private String configValue;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
