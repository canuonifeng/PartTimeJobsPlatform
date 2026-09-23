package com.parttime.cservice.pojo.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Job {
    @Schema(description = "岗位ID")
    private Long id;
    @Schema(description = "企业ID")
    private Long companyId;
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
    @Schema(description = "岗位分类ID")
    private Long categoryId;
    @Schema(description = "薪资类型")
    private String rateType;
    @Schema(description = "薪资金额")
    private BigDecimal rateAmount;
    @Schema(description = "岗位状态")
    private String status;
    @Schema(description = "发布时间")
    private LocalDateTime publishedAt;
    @Schema(description = "招聘人数")
    private Integer headcount;
    @Schema(description = "已录用人数")
    private Integer acceptedCount;
    @Schema(description = "报名截止时间")
    private LocalDateTime deadline;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
    @Schema(description = "关闭原因")
    private String closeReason;
    @Schema(description = "岗位图片")
    private String imageUrl;
    @Schema(description = "自动审核: null跟随平台默认, true开启, false关闭")
    private Boolean autoApprove;
    @Schema(description = "任务类型: WORK-工作, ANNOTATION-标注")
    private String taskType;
    @Schema(description = "计价模式: PER_ITEM-按件, PER_PACKAGE-按包")
    private String pricingMode;
    @Schema(description = "单价")
    private BigDecimal pricePerUnit;
    @Schema(description = "总数量")
    private Integer totalItems;
    @Schema(description = "外部任务ID")
    private String externalTaskId;
    @Schema(description = "外部系统类型")
    private String externalSystemType;
    @Schema(description = "是否自动结算")
    private Boolean autoSettle;
    @Schema(description = "是否急招")
    private Boolean urgent;
}
