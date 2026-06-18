package com.parttime.enterprise.pojo.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class Job {

    @Schema(description = "岗位ID")
    private Long id;
    @Schema(description = "企业ID")
    private Long companyId;
    @Schema(description = "岗位标题")
    private String title;
    @Schema(description = "岗位职责")
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
    @Schema(description = "招聘人数")
    private Integer headcount;
    @Schema(description = "岗位状态: DRAFT-草稿, PUBLISHED-已发布, CLOSED-已关闭")
    private String status;
    @Schema(description = "报名截止时间")
    private LocalDateTime deadline;
    @Schema(description = "岗位图片URL")
    private String imageUrl;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
