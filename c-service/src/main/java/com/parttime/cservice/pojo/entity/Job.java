package com.parttime.cservice.pojo.entity;

import com.parttime.cservice.pojo.vo.JobRateInfoVO;
import com.parttime.cservice.pojo.vo.JobScheduleInfoVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class Job {
    @Schema(description = "岗位ID")
    private Long id;
    @Schema(description = "岗位ID(同id)")
    private Long jobId;
    @Schema(description = "企业ID")
    private Long companyId;
    @Schema(description = "企业名称")
    private String companyName;
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
    @Schema(description = "岗位分类ID")
    private Long categoryId;
    @Schema(description = "分类名称")
    private String categoryName;
    @Schema(description = "薪资类型")
    private String rateType;
    @Schema(description = "薪资金额")
    private BigDecimal rateAmount;
    @Schema(description = "岗位状态")
    private String status;
    @Schema(description = "发布时间")
    private LocalDateTime publishedAt;
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
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    public Job() {}

    public Job(Long id, String title, String description, String location, Long categoryId,
               String categoryName, List<JobRateInfoVO> rates, List<JobScheduleInfoVO> schedules,
               Integer headcount, Integer acceptedCount, LocalDateTime deadline, String status) {
        this.id = id;
        this.jobId = id;
        this.title = title;
        this.description = description;
        this.location = location;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.rates = rates;
        this.schedules = schedules;
        this.headcount = headcount;
        this.acceptedCount = acceptedCount;
        this.deadline = deadline;
        this.status = status;
    }
}
