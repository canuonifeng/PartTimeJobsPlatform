package com.parttime.enterprise.pojo.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class JobTemplate {
    private Long id;
    private Long companyId;
    private String title;
    private String description;
    private Long categoryId;
    private String imageUrl;
    private String province;
    private String city;
    private String district;
    private String address;
    private java.math.BigDecimal latitude;
    private java.math.BigDecimal longitude;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
