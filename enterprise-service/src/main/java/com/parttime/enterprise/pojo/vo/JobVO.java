package com.parttime.enterprise.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import com.parttime.enterprise.enums.JobStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class JobVO {

    @Schema(description = "岗位ID")
    private Long id;
    @Schema(description = "企业ID")
    private Long companyId;
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
    @Schema(description = "岗位分类名称")
    private String categoryName;
    @Schema(description = "招聘人数")
    private Integer headcount;
    @Schema(description = "岗位状态")
    private JobStatus status;
    @Schema(description = "总报名人数")
    private Integer applicationCount;
    @Schema(description = "待审核报名数")
    private Integer pendingApplicationCount;
    @Schema(description = "报名截止时间")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "Asia/Shanghai")
    private LocalDateTime deadline;
    @Schema(description = "薪资规则列表")
    private List<JobRateVO> rates;
    @Schema(description = "排班列表")
    private List<JobScheduleVO> schedules;
    @Schema(description = "岗位图片URL")
    private String imageUrl;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
