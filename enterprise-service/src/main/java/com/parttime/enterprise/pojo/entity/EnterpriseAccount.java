package com.parttime.enterprise.pojo.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class EnterpriseAccount {
    private Long id;
    private Long enterpriseId;
    private String username;
    private String password;
    private String displayName;
    private String role;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
