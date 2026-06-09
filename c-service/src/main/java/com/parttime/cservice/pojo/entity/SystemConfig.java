package com.parttime.cservice.pojo.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SystemConfig {
    private Long id;
    private String configKey;
    private String configValue;
    private String name;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
