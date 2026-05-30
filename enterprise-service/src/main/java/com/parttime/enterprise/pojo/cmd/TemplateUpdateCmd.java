package com.parttime.enterprise.pojo.cmd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class TemplateUpdateCmd {
    @Schema(description = "模版ID")
    private Long id;
    @Schema(description = "职位名称")
    private String title;
    @Schema(description = "职位描述")
    private String description;
    @Schema(description = "分类ID")
    private Long categoryId;
    @Schema(description = "岗位图片URL")
    private String imageUrl;
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
}
