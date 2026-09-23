package com.parttime.cservice.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class JobDetailVO {
    @Schema(description = "岗位ID")
    private Long id;
    @Schema(description = "岗位标题")
    private String title;
    @Schema(description = "岗位描述")
    private String description;
    @Schema(description = "任职要求")
    private String requirements;
    @Schema(description = "岗位联系人姓名")
    private String contactName;
    @Schema(description = "岗位联系方式")
    private String contactPhone;
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
    @Schema(description = "详细地址（街道门牌号）")
    private String address;
    @Schema(description = "纬度")
    private BigDecimal latitude;
    @Schema(description = "经度")
    private BigDecimal longitude;
    @Schema(description = "发布企业")
    private String companyName;
    @Schema(description = "企业logo")
    private String companyLogo;
    @Schema(description = "岗位图片URL")
    private String imageUrl;
    @Schema(description = "分类名称")
    private String categoryName;
    @Schema(description = "薪资规则列表")
    private List<JobRateInfoVO> rates;
    @Schema(description = "排班列表")
    private List<JobScheduleInfoVO> schedules;
    @Schema(description = "招聘人数")
    private Integer headcount;
    @Schema(description = "已录用人数")
    private Integer acceptedCount;
    @Schema(description = "报名截止时间")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "Asia/Shanghai")
    private LocalDateTime deadline;
    @Schema(description = "岗位状态")
    private String status;

    @Schema(description = "任务类型: WORK-工作, ANNOTATION-标注")
    private String taskType;
    @Schema(description = "计价模式: PER_ITEM-按件, PER_PACKAGE-按包")
    private String pricingMode;
    @Schema(description = "单价")
    private BigDecimal pricePerUnit;
    @Schema(description = "总数量")
    private Integer totalItems;

    @Schema(description = "当前工人的申请状态")
    private String applyStatus;
    @Schema(description = "当前工人已报名的排班ID列表")
    private List<Long> appliedScheduleIds;

    @Schema(description = "是否要求技能认证")
    private Boolean certificationRequired;
    @Schema(description = "当前工人是否已具备所需认证")
    private Boolean certified;
    @Schema(description = "是否急招")
    private Boolean urgent;
}
