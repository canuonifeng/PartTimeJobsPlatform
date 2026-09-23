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
    @Schema(description = "岗位标签列表")
    private List<JobTagVO> tags;
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
    @Schema(description = "岗位图片URL")
    private String imageUrl;
    @Schema(description = "距离(公里)")
    private BigDecimal distanceKm;
    @Schema(description = "最低薪资")
    private BigDecimal minRate;
    @Schema(description = "最高薪资")
    private BigDecimal maxRate;
    @Schema(description = "薪资类型列表")
    private List<String> rateTypes;
    @Schema(description = "薪资规则列表")
    private List<JobRateInfoVO> rates;
    @Schema(description = "任务类型: WORK-工作, ANNOTATION-标注")
    private String taskType;
    @Schema(description = "计价模式: PER_ITEM-按件, PER_PACKAGE-按包")
    private String pricingMode;
    @Schema(description = "单价")
    private BigDecimal pricePerUnit;
    @Schema(description = "总数量")
    private Integer totalItems;
    @Schema(description = "已完成数量")
    private Integer itemsCompleted;
    @Schema(description = "是否急招")
    private Boolean urgent;
}
