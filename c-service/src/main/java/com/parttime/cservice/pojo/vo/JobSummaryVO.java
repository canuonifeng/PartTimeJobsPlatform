package com.parttime.cservice.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class JobSummaryVO {
    @Schema(description = "岗位ID")
    private Long id;
    @Schema(description = "岗位标题")
    private String title;
    @Schema(description = "工作地点")
    private String location;
    @Schema(description = "省")
    private String province;
    @Schema(description = "市")
    private String city;
    @Schema(description = "区")
    private String district;
    @Schema(description = "分类名称")
    private String categoryName;
    @Schema(description = "企业名称")
    private String companyName;
    @Schema(description = "企业logo")
    private String companyLogo;
    @Schema(description = "距离(公里)")
    private BigDecimal distanceKm;
    @Schema(description = "最低薪资")
    private BigDecimal minRate;
    @Schema(description = "最高薪资")
    private BigDecimal maxRate;
    @Schema(description = "薪资类型列表")
    private List<String> rateTypes;
}
