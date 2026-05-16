package com.parttime.cservice.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
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
    private LocalDateTime deadline;
    @Schema(description = "岗位状态")
    private String status;
}
