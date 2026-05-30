package com.parttime.enterprise.pojo.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CompanyLocation {
    private Long id;
    private Long companyId;
    private String name;
    private String province;
    private String city;
    private String district;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
