package com.parttime.enterprise.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CompanyLocationVO {
    @Schema(description = "地点ID")
    private Long id;
    @Schema(description = "地点名称")
    private String name;
    @Schema(description = "省")
    private String province;
    @Schema(description = "市")
    private String city;
    @Schema(description = "区")
    private String district;
    @Schema(description = "详细地址")
    private String address;
    @Schema(description = "纬度")
    private BigDecimal latitude;
    @Schema(description = "经度")
    private BigDecimal longitude;
    @Schema(description = "状态 ENABLED/DISABLED")
    private String status;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
